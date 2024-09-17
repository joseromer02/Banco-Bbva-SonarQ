package com.banco.bbva.controller;

import com.banco.dto.CuentaResponseDto;
import com.banco.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"https://localhost:5173", "http://localhost:8080"}, allowCredentials = "true", methods = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RequestMapping("/api/v1/cuentas")
public class    CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @GetMapping("/saldo")
    public ResponseEntity<List<CuentaResponseDto>> obtenerSaldo(@AuthenticationPrincipal User usuario) {
        List<CuentaResponseDto> cuentasDto = cuentaService.getSaldoByEmail(usuario.getUsername());
        if (!cuentasDto.isEmpty()) {
            return ResponseEntity.ok(cuentasDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<CuentaResponseDto> crearCuenta(@AuthenticationPrincipal User usuario) {
        try {
            CuentaResponseDto cuentaDto = cuentaService.crearCuentaParaUsuario(usuario.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(cuentaDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
