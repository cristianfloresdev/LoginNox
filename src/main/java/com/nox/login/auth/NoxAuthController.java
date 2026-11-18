package com.nox.login.auth;

import com.nox.login.biometria.*;
import com.nox.login.dto.LoginRequestDTO;
import com.nox.login.dto.RegisterRequestDTO;
import com.nox.login.entity.User;
import com.nox.login.security.JwtUtil;
import com.nox.login.service.EventoLoginService;
import com.nox.login.service.NoxLoginService;
import com.nox.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/nox-auth")
@RequiredArgsConstructor
public class NoxAuthController
{
    private final UserService userService;
    private final NoxLoginService noxLoginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletRequest httpRequest)
    {
        return noxLoginService.procesarLoginBiomimetico(request, httpRequest);
    }

    /**
     * Endpoint de registro de nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequest)
    {
        userService.guardarUser(registerRequest);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }
}