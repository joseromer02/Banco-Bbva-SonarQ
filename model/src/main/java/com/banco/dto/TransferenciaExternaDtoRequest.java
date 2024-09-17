package com.banco.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferenciaExternaDtoRequest {

    private Long cuentaOrigen; // Cambiado a String

    private String nombreOrigen; // Nombre del banco de origen

    @NotNull(message = "La cuenta destino es obligatoria")
    private String cuentaDestino;

    @NotNull(message = "El nombre del banco destino es obligatorio")
    private String nombreDestino; // Nombre del banco destino

    @NotNull(message = "El código del banco destino es obligatorio")
    private Long codigoBancoDestino;

    @NotNull(message = "El código del banco origen es obligatorio")
    private Long codigoBancoOrigen; // Nuevo campo para el código del banco de origen

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal monto;

    @NotNull(message = "La glosa es obligatoria")
    private String glosa; // Descripción o glosa de la transferencia

}
