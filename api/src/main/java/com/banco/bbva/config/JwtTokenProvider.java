package com.banco.bbva.config;

import com.banco.exception.InvalidJwtAuthenticationException;
import com.banco.exception.NotDataFoundException;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${security.jwt.token.secret-key:Ch4ng1t}")
    private String secretKey = "Ch4ng1t";
    @Value("${security.jwt.token.expire-length:28800000}")
    private int validityInMinutes = 480; // 8h
    @Autowired
    private UserDetailsService userDetailsService;


    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = plusMinutes(now, this.validityInMinutes);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createTokenApi(String username, Date fechaInicio, Date fechaExpiracion) {
        Claims claims = Jwts.claims().setSubject(username);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(fechaInicio)
                .setExpiration(fechaExpiracion)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            try {
                return bearerToken.substring(7);
            } catch (NotDataFoundException e) {
                log.error("Usuario no encontrado");
                return null;
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            Date fechaActual = new Date();
            if (claims.getBody().getExpiration().after(fechaActual)) {
                return true;
            }
            return false;
        } catch (ExpiredJwtException e) {
            log.warn("La sesión del usuario ha expirado");
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error al validar el TOKEN", e);
            throw new InvalidJwtAuthenticationException("Token JWT caducado o no válido.");
        }
    }

    public static Date plusMinutes(Date date, int minutesToAdd) {
        Calendar calDateStart = Calendar.getInstance();
        calDateStart.setTime(date);
        calDateStart.add(Calendar.MINUTE, minutesToAdd);

        return calDateStart.getTime();
    }

}
