package com.banco.service;

import com.banco.dto.CuentaResponseDto;

import java.util.List;

public interface CuentaService {
    List<CuentaResponseDto> getSaldoByEmail(String email);

    CuentaResponseDto crearCuentaParaUsuario(String email);
}
