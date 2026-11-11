package com.nox.login.auth;

import com.nox.login.biometria.*;
import com.nox.login.dto.LoginRequestDTO;
import com.nox.login.entity.User;
import com.nox.login.security.JwtUtil;
import com.nox.login.service.EventoLoginService;
import com.nox.login.service.NoxLoginService;
import com.nox.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/nox-auth")
public class NoxAuthController
{
    @Autowired
    private UserService userService;

    @Autowired
    private EventoLoginService eventoLoginService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NoxLoginService noxLoginService;

    private final NoxDecisionEngine decisionEngine =
            new NoxDecisionEngine();

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest
    )
    {
        // ==========================
        // 1️Observación biomimética
        // ==========================

        HuellaDigitalContextual huella =
                HuellaDigitalContextual.fromRequest(httpRequest);

        PatronComportamiento patron =
                noxLoginService.analizarPatron(
                        request.getEmail(),
                        huella
                );

        NoxLoginService.ResultadoConfianza resultado =
                noxLoginService.evaluarConfianza(
                        huella,
                        patron
                );

        // ==========================
        // 2️⃣ Decisión del cuervo
        // ==========================

        DecisionAcceso decision = decisionEngine.decidir(
                resultado.score(),
                resultado.nivel(),
                patron.getIntentosRecientes()
        );

        if (decision == DecisionAcceso.BLOQUEAR)
        {
            eventoLoginService.registrarFallo(request.getEmail(),
                    httpRequest.getRemoteAddr());

            return ResponseEntity.status(403).body(
                    Map.of("message",
                            "Acceso bloqueado por comportamiento sospechoso")
            );
        }

        if (decision == DecisionAcceso.DESAFIO)
        {
            eventoLoginService.registrarFallo(request.getEmail(),
                    httpRequest.getRemoteAddr());
            return ResponseEntity.status(401).body(
                    Map.of("message",
                            "Se requiere verificación adicional")
            );
        }

        // ==========================
        // 3️⃣ Autenticación clásica
        // ==========================

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token =
                jwtUtil.generarToken(request.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login autorizado por NOX");

        eventoLoginService.registrarExito(request.getEmail(),
                httpRequest.getRemoteAddr());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de registro de nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser)
    {
        userService.guardarUser(newUser);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }
}