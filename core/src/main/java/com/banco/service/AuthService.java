package com.banco.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private RestTemplate restTemplate;

    private String token;

    // Método para obtener el token desde la API
    public String obtenerToken() {
        String url = "https://mls-ubp.azurewebsites.net/api/v1/authUser/login";  // Endpoint de autenticación

        // Crear el cuerpo de la solicitud
        Map<String, String> body = new HashMap<>();
        body.put("username", "bbua");
        body.put("password", "123456");

        // Crear los encabezados
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crear el request entity
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Enviar la solicitud POST
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, String>>() {}
            );

            // Verificar que la respuesta tenga el token
            System.out.println("Respuesta completa del login: " + response.getBody());

            Map<String, String> responseBody = response.getBody();
            if (responseBody != null) {
                token = responseBody.get("token");
                System.out.println("Token obtenido: " + token);
                if (token == null) {
                    throw new RuntimeException("El token no está presente en la respuesta.");
                }
                return token;
            } else {
                throw new RuntimeException("Error en la autenticación: Respuesta vacía.");
            }
        } catch (Exception e) {
            e.printStackTrace();  // Traza detallada del error
            throw new RuntimeException("Error al autenticar: " + e.getMessage());
        }
    }

    // Método para obtener el token almacenado o generar uno nuevo
    public String getTokenApiAzure() {
        if (token == null) {
            log.info("Token api azure nulo");
            return obtenerToken();  // Si no hay token, obtenerlo
        }
        System.out.println("Token ya almacenado: " + token);
        return token;
    }

    // Método para forzar la actualización del token
    public void renovarToken() {
        this.token = obtenerToken();
    }
}
