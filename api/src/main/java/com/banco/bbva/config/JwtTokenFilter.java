package com.banco.bbva.config;

import com.banco.exception.InvalidJwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Serializable;

@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter implements Serializable {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            if (servletRequest.getServletPath().contains("/api/v1/auth") || servletRequest.getServletPath().contains("/api/v1/usuarios")) {
                filterChain.doFilter(servletRequest, response);
                return;
            }

            String token = jwtTokenProvider.resolveToken(((HttpServletRequest) servletRequest).getHeader("Authorization"));
            if (token == null) {
//                log.info("Request Sin Token: " + ((HttpServletRequest) servletRequest).getRequestURL());
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    if (auth != null) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                log.error("No se logró validar el JWT, por lo que se devuelve código 403. FORBIDDEN");
                response.setContentType(MediaType.APPLICATION_JSON.getType());
                response.getWriter().write(new ObjectMapper().writeValueAsString(HttpStatus.FORBIDDEN));
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }
            log.error("No se logró validar el JWT, por lo que se devuelve código 403. FORBIDDEN");
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.getWriter().write(new ObjectMapper().writeValueAsString(HttpStatus.FORBIDDEN));
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } catch (InvalidJwtAuthenticationException e) {
            log.error("Exepción al validar el JWT", e);
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.getWriter().write(new ObjectMapper().writeValueAsString(HttpStatus.FORBIDDEN));
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } catch (Exception e) {
            log.error("Se generó una exepción genérica al validar el JWT", e);
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            response.getWriter().write(new ObjectMapper().writeValueAsString(HttpStatus.INTERNAL_SERVER_ERROR));
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
