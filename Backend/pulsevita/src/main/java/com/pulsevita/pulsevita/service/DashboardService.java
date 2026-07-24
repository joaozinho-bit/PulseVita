package com.pulsevita.pulsevita.service;

import com.pulsevita.pulsevita.controller.DashboardDTO;
import com.pulsevita.pulsevita.model.HistoricoPaciente;
import com.pulsevita.pulsevita.model.Paciente;
import com.pulsevita.pulsevita.repository.HistoricoPacienteRepository;
import com.pulsevita.pulsevita.repository.PacienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Monta o resumo do dashboard reutilizando a logica que ja existe:
//  - a proxima consulta vem do ConsultaService (mesma regra do resto da app);
//  - a ultima medicao vem do HistoricoPacienteRepository.
// Nao duplica regras de negocio: apenas compoe e formata para o ecra inicial.
@Service
public class DashboardService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final PacienteRepository pacienteRepository;
    private final ConsultaService consultaService;
    private final HistoricoPacienteRepository historicoRepository;

    public DashboardService(PacienteRepository pacienteRepository,
                            ConsultaService consultaService,
                            HistoricoPacienteRepository historicoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaService = consultaService;
        this.historicoRepository = historicoRepository;
    }

    public DashboardDTO montar(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Paciente não encontrado."));

        DashboardDTO dto = new DashboardDTO();
        dto.utilizador = utilizador(paciente);

        // proxima consulta confirmada (mesma regra usada no resto da app)
        consultaService.proximaConsulta(idPaciente).ifPresent(c -> {
            DashboardDTO.ProximaConsulta pc = new DashboardDTO.ProximaConsulta();
            pc.data = c.getDataConsulta() != null ? c.getDataConsulta().format(DATA) : null;
            pc.hora = c.getHoraConsulta() != null ? c.getHoraConsulta().format(HORA) : null;
            if (c.getMedico() != null) {
                pc.medico = c.getMedico().getNome();
                pc.especialidade = c.getMedico().getEspecializacao();
            }
            pc.estado = c.getEstado();
            dto.proximaConsulta = pc;
        });

        HistoricoPaciente ultima = historicoRepository.findTopByIdPacienteOrderByDataLeituraDesc(idPaciente);
        if (ultima != null && ultima.getDataLeitura() != null) {
            LocalDateTime leitura = ultima.getDataLeitura();
            DashboardDTO.Medicao m = new DashboardDTO.Medicao();
            m.data = leitura.toLocalDate().format(DATA);
            m.hora = leitura.toLocalTime().format(HORA);
            m.temperatura = ultima.getTemperatura();
            m.bpm = ultima.getBpm();
            m.avaliacao = ultima.getAvaliacao();
            m.tipoMedicao = ultima.getTipoMedicao() != null ? ultima.getTipoMedicao().name() : null;
            dto.ultimaMedicao = m;
            dto.medicaoDeHoje = leitura.toLocalDate().isEqual(LocalDate.now());
        }

        return dto;
    }

    private DashboardDTO.Utilizador utilizador(Paciente paciente) {
        DashboardDTO.Utilizador u = new DashboardDTO.Utilizador();
        u.nome = paciente.getNomeCompleto();
        u.nPaciente = paciente.getN_paciente();
        u.fotoPerfil = paciente.getFotoPerfil();
        return u;
    }
}
