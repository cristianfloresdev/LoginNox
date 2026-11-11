package com.nox.login.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/nox")
public class NoxProtectedController
{
    @GetMapping("/status")
    public ResponseEntity<?> status(Principal principal)
    {
        return ResponseEntity.ok(Map.of(
                "message", "Acceso autorizado por NOX",
                "usuario", principal.getName()
        ));
    }
}


