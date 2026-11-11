package com.nox.login.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Clase utilitaria para generar y validar tokens JWT.
 * La clave secreta se carga desde application.properties para que
 * sea estable entre reinicios y no se genere aleatoriamente.
 */
@Component
public class JwtUtil {

    // ✅ Clave fija desde application.properties
    // En application.properties agrega:
    //   jwt.secret=unaClaveSeguraDeMinimo32CaracteresAqui1234
    //   jwt.expiration=3600000
    @Value("${jwt.secret}")
    private String secretString;

    // ✅ Duración también configurable externamente
    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * Construye la SecretKey a partir del string configurado.
     * Se llama en cada operación para garantizar consistencia.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT firmado con el email del usuario.
     */
    public String generarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Valida si el token es auténtico y no ha expirado.
     */
    public boolean validarToken(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Token malformado, expirado o firma inválida
            return false;
        }
    }

    /**
     * Obtiene el email (subject) del token.
     */
    public String obtenerEmailDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    /**
     * Extrae los claims internos del token verificando la firma.
     */
    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}