package com.nox.login.biometria;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * PatronComportamiento
 * --------------------
 * Analiza el comportamiento histórico reciente de intentos de acceso.
 * No persiste. Memoria limitada (biomimética).
 */
public class PatronComportamiento
{

    private static final int MAX_EVENTOS = 20;
    private static final int VENTANA_MINUTOS = 10;

    private final Deque<EventoAcceso> eventos = new ArrayDeque<>();

    /**
     * Registra un nuevo intento de acceso.
     */
    public void registrarIntento(HuellaDigitalContextual huella)
    {
        limpiarEventosAntiguos();
        eventos.addLast(new EventoAcceso(huella, LocalDateTime.now()));

        if (eventos.size() > MAX_EVENTOS)
        {
            eventos.removeFirst();
        }
    }

    /**
     * Detecta si hay comportamiento anómalo.
     */
    public boolean hayAnomalia()
    {
        if (eventos.size() < 3)
        {
            return false; // No hay suficiente información
        }

        return hayFrecuenciaExcesiva()
                || hayCambioBruscoDeHuella();
    }

    // =========================
    // REGLAS DE DETECCIÓN
    // =========================

    private boolean hayFrecuenciaExcesiva()
    {
        LocalDateTime ahora = LocalDateTime.now();

        long recientes = eventos.stream()
                .filter(e -> Duration.between(e.timestamp(), ahora).toMinutes() <= 1)
                .count();

        return recientes >= 5;
    }

    private boolean hayCambioBruscoDeHuella()
    {
        String huellaBase = eventos.peekFirst().huella().getDeviceFingerprint();

        return eventos.stream()
                .anyMatch(e -> !e.huella().getDeviceFingerprint().equals(huellaBase));
    }

    private void limpiarEventosAntiguos()
    {
        LocalDateTime ahora = LocalDateTime.now();

        while (!eventos.isEmpty())
        {
            EventoAcceso primero = eventos.peekFirst();
            if (Duration.between(primero.timestamp(), ahora).toMinutes() > VENTANA_MINUTOS)
            {
                eventos.removeFirst();
            }
            else
            {
                break;
            }
        }
    }

    // =========================
    // EVENTO INTERNO
    // =========================

    private record EventoAcceso(
            HuellaDigitalContextual huella,
            LocalDateTime timestamp
    ) {}

    public int getIntentosRecientes()
    {
        return eventos.size();
    }

    public HuellaDigitalContextual getUltimaHuella()
    {
        return eventos.isEmpty() ? null : eventos.peekLast().huella();
    }
}