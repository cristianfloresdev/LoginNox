package com.nox.login.biometria;

/**
 * NoxDecisionEngine
 * ------------------
 * Motor de decision del cuervo(cerebro)
 *
 * Recibe información procesada si:
 * - Score biomimetico
 * - Nivel de confianza
 * - Número de intentos recientes
 *
 * Devuelve una decision clara y explicita:
 * DecisionAcceso
 */
public class NoxDecisionEngine
{
    /**
     * Decide la accion de acceso a tomar
     */
    private static final int INTENTOS_MAXIMOS = 5;

    public DecisionAcceso decidir(
            int score,
            NivelConfianza nivel,
            int intentosRecientes
    )
    {

        // =========================
        // BLOQUEO AGRESIVO
        // =========================
        //Si hay muchos intentos y la confianza es baja,
        //se asume ataque activo
        if (intentosRecientes >= INTENTOS_MAXIMOS && nivel == NivelConfianza.BAJA)
        {
            return DecisionAcceso.BLOQUEAR;
        }

        // =========================
        // DESAFÍO
        // =========================
        //Acceso con incertidumbre:
        //Se permite continuar pero con verificacion adicional
        if (nivel == NivelConfianza.MEDIA)
        {
            return DecisionAcceso.DESAFIO;
        }

        // =========================
        // ACCESO NORMAL
        // =========================
        //Confianza suficiente, comportamiento esperado
        return DecisionAcceso.PERMITIR;
    }
}
