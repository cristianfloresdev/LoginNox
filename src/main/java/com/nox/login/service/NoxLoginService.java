package com.nox.login.service;

import com.nox.login.biometria.*;
import com.nox.login.dto.LoginRequestDTO;
import com.nox.login.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NoxLoginService
{
    /**
     * Memoria biomimética:
     * un patrón por usuario (no persistente)
     */
    private final Map<String, PatronComportamiento> patronesPorUsuario =
            new ConcurrentHashMap<>();

    private final EventoLoginService eventoLoginService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final NoxDecisionEngine decisionEngine;

    /**
     * Analiza y registra el patrón de comportamiento
     */
    public PatronComportamiento analizarPatron(
            String email,
            HuellaDigitalContextual huella
    )
    {
        PatronComportamiento patron =
                patronesPorUsuario.computeIfAbsent(
                        email,
                        k -> new PatronComportamiento()
                );

        patron.registrarIntento(huella);
        return patron;
    }

    /**
     * Calcula el score de confianza
     */
    public ResultadoConfianza evaluarConfianza(
            HuellaDigitalContextual huella,
            PatronComportamiento patron
    )
    {
        ScoreConfianza calculadora = new ScoreConfianza();

        int score = calculadora.calcular(huella, patron);
        NivelConfianza nivel = calculadora.clasificar(score);

        return new ResultadoConfianza(score, nivel);
    }

    /**
     * Resultado inmutable del análisis
     */
    public record ResultadoConfianza(
            int score,
            NivelConfianza nivel
    ) {}

    ////////////////////////
    public ResponseEntity<?> procesarLoginBiomimetico(LoginRequestDTO request, HttpServletRequest httpRequest)
    {
        // ==========================
        // A Observación biomimética
        // ==========================
        HuellaDigitalContextual huella = HuellaDigitalContextual.fromRequest(httpRequest);

        // Usamos "this" porque ya estamos dentro del servicio
        PatronComportamiento patron = this.analizarPatron(request.getEmail(), huella);
        ResultadoConfianza resultado = this.evaluarConfianza(huella, patron);

        // ==========================
        // B Decisión del cuervo
        // ==========================
        DecisionAcceso decision = decisionEngine.decidir(
                resultado.score(),
                resultado.nivel(),
                patron.getIntentosRecientes()
        );

        if (decision == DecisionAcceso.BLOQUEAR) {
            eventoLoginService.registrarFallo(request.getEmail(), httpRequest.getRemoteAddr());
            return ResponseEntity.status(403).body(Map.of("message", "Acceso bloqueado por comportamiento sospechoso"));
        }

        if (decision == DecisionAcceso.DESAFIO) {
            eventoLoginService.registrarFallo(request.getEmail(), httpRequest.getRemoteAddr());
            return ResponseEntity.status(401).body(Map.of("message", "Se requiere verificación adicional"));
        }

        // ==========================
        // C Autenticación clásica
        // ==========================
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generarToken(request.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login autorizado por NOX");

        eventoLoginService.registrarExito(request.getEmail(), httpRequest.getRemoteAddr());

        return ResponseEntity.ok(response);
    }
}

