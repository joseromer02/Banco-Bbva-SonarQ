package com.banco.bbva.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "APIs de Banco BBVA",
                version = "v1",
                description = "Esta aplicación provee APIs REST para la aplicación web de transferencias del Banco BBVA",
                contact = @Contact(
                        name = "Mateo González",
                        email = "mateogon1906@gmail.com"
                )
        ),
        servers = {
                @Server(
                        url = "https://localhost:8080",
                        description = "Servidor de desarrollo"
                ),
                @Server(
                url = "https://bbva-api-upb.azurewebsites.net",
                description = " Servidor de desarrollo"
                )
        }
)
@SecurityScheme(
        name = "bearerToken",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "jwt"
)
public class SwaggerConfig {
}
