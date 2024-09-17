package com.banco.repository;

import com.banco.entity.Banco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BancoRepository extends JpaRepository<Banco, Long> {

    Optional<Banco> findByCodigo(Integer codigo);
}
