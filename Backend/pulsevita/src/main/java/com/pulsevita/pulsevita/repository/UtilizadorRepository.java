package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
    Utilizador findByEmail(String email); //buca utilizador por email 
}