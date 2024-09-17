package com.banco.bbva.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class ContentTypeInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        // Validar el Content-Type
        String contentType = response.getHeaders().getFirst("Content-Type");
        if (contentType != null && !contentType.contains("/")) {
            // Corrigir el Content-Type malformado
            response.getHeaders().set("Content-Type", "application/json;charset=ISO-8859-1");
        }

        return response;
    }
}

