package com.banco.bbva.controller;

import com.banco.dto.ChangePasswordDto;
import com.banco.dto.UsuarioRequestDto;
import com.banco.dto.UsuarioResponseDto;
import com.banco.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = {"https://localhost:5173", "http://localhost:8080"}, allowCredentials = "true", methods = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> createUsuario(@RequestBody UsuarioRequestDto usuarioRequestDto) {
        log.info("Creando usuario");
        UsuarioResponseDto usuarioResponseDto = usuarioService.saveUsuario(usuarioRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponseDto);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nueva contraseña y confirmación no coinciden.");
        }
        boolean result = usuarioService.changePassword(email, changePasswordDto.getNewPassword());
        if (result) {
            return ResponseEntity.ok("Contraseña cambiada exitosamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al cambiar la contraseña.");
        }
    }
}
