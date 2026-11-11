package com.nox.login.service;

import com.nox.login.biometria.HuellaDigitalContextual;
import com.nox.login.biometria.ScoreConfianza;
import com.nox.login.biometria.NivelConfianza;
import com.nox.login.biometria.PatronComportamiento;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NoxLoginService
{
    /**
     * Memoria biomimética:
     * un patrón por usuario (no persistente)
     */
    private final Map<String, PatronComportamiento> patronesPorUsuario =
            new ConcurrentHashMap<>();

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
}
