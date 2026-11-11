package com.nox.login.biometria;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Representa la huella digital, contextual de un intento de acceso
 *
 * Su funcion es observar y describir el contexto
 * en el que ocurre un login
 *
 * //! Aquí es donde el CUERVO MIRA EL ENTORNO
 */
public class HuellaDigitalContextual
{
    //Hash de la IP(No se guarda la IP real -> privacidad)
    private final String ipHash;

    //Hash del user-Agent (Navegador / sistema)
    private final String userAgentHash;

    //Huella combinada IP + User-Agent
    private final String deviceFingerprint;

    //Hora del intento de login
    private final int loginHour;

    //Dia de la semana (1 = lunes, 7 = domingo)
    private final int dayOfWeek;

    /**
     * Constructor privado conceptual:
     * La huella No debe construirse manualmente, solo a partir del entorno real
     */
    public HuellaDigitalContextual(String ipHash, String userAgentHash, String deviceFingerprint, int loginHour, int dayOfWeek)
    {
        this.ipHash = ipHash;
        this.userAgentHash = userAgentHash;
        this.deviceFingerprint = deviceFingerprint;
        this.loginHour = loginHour;
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Punto unico de creacion
     * aquí el cuervo observa el entorno
     * No decide
     * No bloquea
     * No pregunta a security
     *
     * Solo recolecta senales del entorno
     */
    public static HuellaDigitalContextual fromRequest(HttpServletRequest request)
    {
        String ip = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        LocalDateTime now = LocalDateTime.now();

        return new HuellaDigitalContextual(
                hash(ip),
                hash(userAgent),
                generateDeviceFingerprint(ip, userAgent),
                now.getHour(),
                now.getDayOfWeek().getValue()
        );
    }

    // ===========================================
    // MÉTODOS PRIVADOS (Recoleccion de senales)
    // ===========================================

    //Extrae la IP real considerando proxies o balanceadores
    private static String extractClientIp(HttpServletRequest request)
    {
        String forwarded = request.getHeader("X-Forwarded-For");

        if(forwarded != null && !forwarded.isBlank())
        {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    // Genera una huella de dispositivo
    // combinando IP + User-Agent.
    private static String generateDeviceFingerprint(String ip, String userAgent)
    {
        return hash(ip + "::" + userAgent);
    }

    //Hash SHA-256 para evitar almacenar datos sensibles
    private static String hash(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedHash);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Error generando hash biomimetico", e);
        }
    }

    // =================================
    // SENALES DE COMPORTAMIENTO
    // =================================

    // Detecta horarios inusuales de acceso
    // Senal debil (no bloquea por si sola)
    public boolean isHorarioInusual()
    {
        // Horario típico humano: 6 a 22
        return loginHour < 6 || loginHour > 22;
    }

    //Detecta dias inusuales(fin de semana) senal debil
    public boolean isDiaInusual()
    {
        // 6 = sábado, 7 = domingo
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    /**
     * Señal débil:
     * si cambia mucho el fingerprint suele indicar
     * VPN, proxy o automatización
     */
    public boolean isDispositivoInestable(String fingerprintEsperado)
    {
        return !this.deviceFingerprint.equals(fingerprintEsperado);
    }

    // =================================
    // GETTER (solo lectura)
    // =================================


    public String getIpHash()
    {
        return ipHash;
    }

    public String getUserAgentHash()
    {
        return userAgentHash;
    }

    public String getDeviceFingerprint()
    {
        return deviceFingerprint;
    }

    public int getLoginHour()
    {
        return loginHour;
    }

    public int getDayOfWeek()
    {
        return dayOfWeek;
    }


}
