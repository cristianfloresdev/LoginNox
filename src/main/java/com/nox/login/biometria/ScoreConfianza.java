package com.nox.login.biometria;

/**
 * ScoreConfianza
 * --------------
 * Calcula el nivel de confianza de un intento de acceso.
 *
 * Transforma señales ya recolectadas en un score numérico y semántico.
 * Es la capa de evaluación del cuervo.
 *
 * JERARQUÍA DE SEÑALES:
 * - Señales fuertes:  alteran el score significativamente por sí solas
 * - Señales débiles:  solo penalizan cuando hay contexto anómalo de fondo
 */
public class ScoreConfianza {

    // Puntaje máximo posible
    private static final int SCORE_MAX = 100;

    // =====================
    // PESOS — señales fuertes
    // =====================
    private static final int PESO_ANOMALIA_PATRON     = 35; // Comportamiento histórico anómalo
    private static final int PESO_DISPOSITIVO_INESTABLE = 25; // Cambio de dispositivo / User-Agent

    // =====================
    // PESOS — señales débiles
    // Solo se aplican si hay una señal fuerte activa
    // =====================
    private static final int PESO_HORARIO_INUSUAL     = 15; // Acceso fuera de horario humano típico
    private static final int PESO_DIA_INUSUAL         = 10; // Acceso en fin de semana

    /**
     * Calcula el score biomimético total.
     *
     * Lógica de penalización:
     * 1. Las señales fuertes penalizan siempre que se detectan.
     * 2. Las señales débiles solo penalizan si al menos una señal fuerte está activa,
     *    evitando falsos positivos para usuarios con hábitos no convencionales.
     *
     * @param huella  contexto observado del acceso actual
     * @param patron  patrón histórico de comportamiento del usuario
     * @return score normalizado entre 0 y 100
     */
    public int calcular(
            HuellaDigitalContextual huella,
            PatronComportamiento patron
    )
    {
        int score = SCORE_MAX;

        // =============================================
        // SEÑALES FUERTES (penalizan por sí solas)
        // =============================================

        boolean hayAnomaliaPatron = patron.hayAnomalia();
        boolean hayDispositivoInestable = false;

        // Verificar cambio de dispositivo respecto a la última huella conocida
        HuellaDigitalContextual ultimaHuella = patron.getUltimaHuella();
        if (ultimaHuella != null)
        {
            hayDispositivoInestable = huella.isDispositivoInestable(
                    ultimaHuella.getDeviceFingerprint()
            );
        }

        if (hayAnomaliaPatron)
        {
            score -= PESO_ANOMALIA_PATRON;
        }

        if (hayDispositivoInestable)
        {
            score -= PESO_DISPOSITIVO_INESTABLE;
        }

        // =============================================
        // SEÑALES DÉBILES
        // Solo penalizan si hay al menos una señal fuerte activa.
        // Esto evita bloquear a usuarios legítimos con horarios no convencionales.
        // =============================================

        boolean haySeñalFuerte = hayAnomaliaPatron || hayDispositivoInestable;

        if (haySeñalFuerte)
        {
            if (huella.isHorarioInusual())
            {
                score -= PESO_HORARIO_INUSUAL;
            }

            if (huella.isDiaInusual())
            {
                score -= PESO_DIA_INUSUAL;
            }
        }

        // =============================================
        // NORMALIZACIÓN — evita valores negativos
        // =============================================

        return Math.max(score, 0);
    }

    /**
     * Traduce el score numérico a una clasificación semántica.
     *
     * >= 80 → ALTA   : comportamiento normal, acceso fluido
     * >= 50 → MEDIA  : incertidumbre, posible desafío adicional
     *  < 50 → BAJA   : señales de riesgo, posible bloqueo
     */
    public NivelConfianza clasificar(int score)
    {
        if (score >= 80) return NivelConfianza.ALTA;
        if (score >= 50) return NivelConfianza.MEDIA;
        return NivelConfianza.BAJA;
    }
}