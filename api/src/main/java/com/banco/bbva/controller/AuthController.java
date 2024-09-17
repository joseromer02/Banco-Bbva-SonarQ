package com.banco.bbva.controller;

import com.banco.bbva.config.JwtTokenProvider;
import com.banco.dto.AuthenticationDto;
import com.banco.dto.OKAuthDto;
import com.banco.entity.Usuario;
import com.banco.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "auth", description = "API para procesos de autentificación")
@Slf4j
@RestController("authControllerImpl")
@CrossOrigin(origins = {"https://localhost:5173", "http://localhost:8080"}, allowCredentials = "true", methods = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UsuarioService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<OKAuthDto> signin(@RequestBody AuthenticationDto data) {
        try {
            String token = validateAuthData(data);
            log.info("Sesión iniciada por el usuario: {}", data.getCorreo());
            return ok(OKAuthDto.builder()
                    .correo(data.getCorreo())
                    .token(token)
                    .build());
        } catch (Exception e) {
            log.error("Error al autentificar el usuario: {}", data.getCorreo(), e);
            throw e;
        }
    }

    private String validateAuthData(AuthenticationDto data) {
        String correo = data.getCorreo();
        Usuario authAuthUser;
        try {
            authAuthUser = usuarioService.findByCorreo(data.getCorreo());
            if (authAuthUser == null) {
                throw new BadCredentialsException("Correo " + correo + "no encontrado.");
            }
        } catch (Exception e) {
            log.error("No se encontró el correo " + correo + " registrado en la base de datos");
            throw e;
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(data.getCorreo(), data.getContrasena()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return jwtTokenProvider.createToken(data.getCorreo());
        } catch (AuthenticationException e) {
            log.error("Error al verificar las credenciales", e);
            throw new BadCredentialsException("Las credenciales son incorrectas");
        } catch (Exception e) {
            log.error("Error de autentificacion: ", e);
            throw e;
        }
    }

    @GetMapping("/google")
    public void googleLogin(OAuth2AuthenticationToken authentication, HttpServletResponse response) throws IOException {
        String token = jwtTokenProvider.createToken(authentication.getName());
        String redirectUrl = "https://localhost:5173/?token=" + token;
        response.sendRedirect(redirectUrl);  // Redirigir al frontend con el token JWT
    }

}
