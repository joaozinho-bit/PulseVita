package com.pulsevita.pulsevita.repository;

import com.pulsevita.pulsevita.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Medico findByNumeroMedico(String numeroMedico); // login sem cartão

    Medico findByIdCartao(String idCartao); // login com cartão RFID
}