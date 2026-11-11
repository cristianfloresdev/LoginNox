package com.nox.login.biometria;

/**
 * Enum que representa el nivel de confianza(el cuervo) tiene sobre un acceso
 *
 * Este enum es una clasificacion intermedia,
 * usada durante el analysis biometric / conductal.
 */
public enum NivelConfianza
{
    /**
     * Nivel de confianza alto si:
     *
     * - El comportamiento coincide con el historial del usuario
     * - IP, dispositivo y patron son consistentes
     * - No existen senales de ataque
     */
    ALTA,

    /**
     * Nivel de confianza medio si:
     *
     * - Existen variaciones leves en el comportamiento
     * - El acceso no es claramente malicioso
     * - Puede tratarse de un cambio legítimo
     */
    MEDIA,

    /**
     * Nivel de confianza bajo si:
     *
     * - El patron es claramente anomalo
     * - se detectan senales de riesgo
     * - coincide con comportamientos sospechosos previos
     */
    BAJA
}
