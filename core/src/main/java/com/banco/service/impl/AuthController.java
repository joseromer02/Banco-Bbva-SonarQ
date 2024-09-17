package com.banco.service.impl;

import com.banco.dto.LoginDto;
import com.banco.service.AuthService;
import com.banco.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Service("authServiceImpl")
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            // Obtener el token
            String token = authService.obtenerToken();

            // Guardar el token en el servicio
            tokenService.setToken(token);

            return ResponseEntity.ok("Login exitoso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al iniciar sesión: " + e.getMessage());
        }
    }
}
