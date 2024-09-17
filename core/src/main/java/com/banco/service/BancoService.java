package com.banco.service;

import com.banco.dto.BancoDto;
import com.banco.entity.Banco;
import com.banco.repository.BancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class BancoService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthService tokenService;

    @Autowired
    private BancoRepository bancoRepository;

    // Método para sincronizar bancos
    @Scheduled(fixedRate = 3600000)  // Se ejecutará cada hora
    public void sincronizarBancos() {
        String url = "https://mls-ubp.azurewebsites.net/api/v1/bancos";

        // Obtener el token del servicio
        String token = tokenService.getTokenApiAzure(); //token de api externa

        System.out.println("Token utilizado para sincronización: " + token);  // Verifica el token

        // Crear los encabezados
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);  // Agregar el token al header
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Enviar la solicitud GET
            ResponseEntity<BancoDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BancoDto[].class
            );

            // Validar el tipo de contenido antes de procesar la respuesta
            String contentType = response.getHeaders().getContentType().toString();
            System.out.println("Content-Type recibido: " + contentType);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                BancoDto[] bancosDto = response.getBody();
                for (BancoDto bancoDto : bancosDto) {
                    // Convertir BancoDto a Banco
                    Banco banco = convertirDtoAEntidad(bancoDto);
                    // Guardar los bancos en la base de datos local
                    bancoRepository.save(banco);
                }
            } else {
                System.out.println("Error al sincronizar los bancos: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            // Si hay un error 403, intentamos renovar el token
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("Token expirado o inválido, renovando...");
                tokenService.renovarToken();  // Forzamos la renovación del token
                sincronizarBancos();  // Reintentamos la sincronización
            } else {
                System.out.println("Error al sincronizar los bancos: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error inesperado al sincronizar los bancos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Banco convertirDtoAEntidad(BancoDto bancoDto) {
        Banco banco = new Banco();
        banco.setNombre(bancoDto.getNombre());
        banco.setCodigo(bancoDto.getCodigo());
        return banco;
    }
}
