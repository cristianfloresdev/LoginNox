package com.nox.login.biometria;

/**
 * Enum que representa las posibles decisiones
 * que puede tomar el sistema de biometric / seguridad adaptativa
 * el cuervo después de analizar el contexto de un acceso
 *
 * Su proposito es definir el resultado final de la evaluacion de riesgo realizada por el cuervo
 */
public enum DecisionAcceso
{
    /**
     * El acceso es considerado seguro sí:
     *
     * - El comportamiento del usuario es normal
     * - No hay senales de riesgo
     * - Coincide IP, dispositivo, patron etc.
     *
     * El sistema permite continuar sin friccion
     */
    PERMITIR,

    /**
     * El acceso sospechoso, pero no critico si:
     *
     * - Cambios leves de IP
     * - Nuevo dispositivo
     * - Patron parcialmente diferente
     *
     * El sistema debe aplicar un reto adicional
     * (captcha, código, validacion secundaria).
     */
    DESAFIO,

    /**
     * El acceso es considerado de alto riesgo si:
     *
     * - Patrones anomalos
     * - Demasiados intentos fallidos
     * - Comportamiento claramente malicioso
     */
    BLOQUEAR
}
