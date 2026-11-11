package com.nox.login.service;

import com.nox.login.evento.EventoLogin;
import com.nox.login.repository.EventoLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventoLoginService
{
    @Autowired
    private EventoLoginRepository eventoLoginRepository;

    public void registrarExito(String email, String ip)
    {
        EventoLogin evento = new EventoLogin(email, ip, true);
        eventoLoginRepository.save(evento);
    }

    public void registrarFallo(String email, String ip)
    {
        EventoLogin evento = new EventoLogin(email, ip, false);
        eventoLoginRepository.save(evento);
    }
}