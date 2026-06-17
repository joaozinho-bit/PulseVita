let mesAtualMarcacoes = 4;
let anoAtualMarcacoes = 2026;

let diaSelecionadoMarcacoes = null;
let horarioSelecionadoMarcacoes = null;
let pacienteSelecionadoMarcacoes = null;

const horariosMarcacoes = [
    "09:00",
    "10:00",
    "11:00",
    "12:00",
    "13:00",
    "14:00",
    "15:00",
    "16:00"
];

const nomesMesesMarcacoes = [
    "Janeiro",
    "Fevereiro",
    "Março",
    "Abril",
    "Maio",
    "Junho",
    "Julho",
    "Agosto",
    "Setembro",
    "Outubro",
    "Novembro",
    "Dezembro"
];

const marcacoesIniciaisMedico = [
    {
        id: 1,
        data: "2026-05-09",
        hora: "09:00",
        estado: "pendente",
        solicitadoEm: "05/05/2026 às 09:32",
        motivo: "Consulta de rotina",
        paciente: {
            nome: "Ana Paula",
            idade: 27,
            genero: "Feminino",
            telefone: "928 345 776",
            email: "ana.paula@gmail.com"
        }
    },
    {
        id: 2,
        data: "2026-05-09",
        hora: "09:00",
        estado: "pendente",
        solicitadoEm: "05/05/2026 às 10:15",
        motivo: "Batimentos cardíacos altos",
        paciente: {
            nome: "André Silva",
            idade: 31,
            genero: "Masculino",
            telefone: "934 225 901",
            email: "andre.silva@gmail.com"
        }
    },
    {
        id: 3,
        data: "2026-05-18",
        hora: "11:00",
        estado: "pendente",
        solicitadoEm: "07/05/2026 às 14:22",
        motivo: "Temperatura elevada",
        paciente: {
            nome: "Marta Lopes",
            idade: 24,
            genero: "Feminino",
            telefone: "919 456 783",
            email: "marta.lopes@gmail.com"
        }
    },
    {
        id: 4,
        data: "2026-05-18",
        hora: "11:00",
        estado: "pendente",
        solicitadoEm: "07/05/2026 às 15:08",
        motivo: "Dores de cabeça constantes",
        paciente: {
            nome: "Diogo Santos",
            idade: 29,
            genero: "Masculino",
            telefone: "961 123 456",
            email: "diogo.santos@gmail.com"
        }
    },
    {
        id: 5,
        data: "2026-05-25",
        hora: "14:00",
        estado: "recusado",
        solicitadoEm: "09/05/2026 às 12:40",
        motivo: "Consulta geral",
        paciente: {
            nome: "Joana Ferreira",
            idade: 35,
            genero: "Feminino",
            telefone: "933 987 111",
            email: "joana.ferreira@gmail.com"
        }
    },
    {
        id: 6,
        data: "2026-05-30",
        hora: "14:00",
        estado: "pendente",
        solicitadoEm: "12/05/2026 às 08:50",
        motivo: "Resultados instáveis nas medições",
        paciente: {
            nome: "Rui Costa",
            idade: 42,
            genero: "Masculino",
            telefone: "912 789 333",
            email: "rui.costa@gmail.com"
        }
    },
    {
        id: 7,
        data: "2026-05-31",
        hora: "15:00",
        estado: "pendente",
        solicitadoEm: "13/05/2026 às 11:03",
        motivo: "Avaliação médica",
        paciente: {
            nome: "Carolina Martins",
            idade: 22,
            genero: "Feminino",
            telefone: "968 333 222",
            email: "carolina.martins@gmail.com"
        }
    }
];

function carregarMarcacoesMedico() {
    const dadosGuardados = localStorage.getItem("pulsevita_marcacoes_medico");

    if (dadosGuardados) {
        return JSON.parse(dadosGuardados);
    }

    localStorage.setItem(
        "pulsevita_marcacoes_medico",
        JSON.stringify(marcacoesIniciaisMedico)
    );

    return JSON.parse(JSON.stringify(marcacoesIniciaisMedico));
}

let marcacoesMedico = carregarMarcacoesMedico();

function guardarMarcacoesMedico() {
    localStorage.setItem(
        "pulsevita_marcacoes_medico",
        JSON.stringify(marcacoesMedico)
    );
}

function iniciarMarcacoesMedico() {
    const calendario = document.getElementById("calendarioMarcacoes");

    if (!calendario) {
        return;
    }

    const btnAnterior = document.getElementById("btnMesAnteriorMarcacoes");
    const btnSeguinte = document.getElementById("btnMesSeguinteMarcacoes");

    btnAnterior.addEventListener("click", voltarMesMarcacoes);
    btnSeguinte.addEventListener("click", avancarMesMarcacoes);

    renderizarMarcacoesMedico();
}

function renderizarMarcacoesMedico() {
    renderizarCalendarioMarcacoes();
    renderizarHorariosMarcacoes();
    renderizarPacientesMarcacoes();
    renderizarDadosPacienteMarcacoes();
}

function renderizarCalendarioMarcacoes() {
    const calendario = document.getElementById("calendarioMarcacoes");
    const tituloMes = document.getElementById("tituloMesMarcacoes");

    calendario.innerHTML = "";

    tituloMes.textContent = `${nomesMesesMarcacoes[mesAtualMarcacoes]} - ${anoAtualMarcacoes}`;

    const totalDiasMes = new Date(anoAtualMarcacoes, mesAtualMarcacoes + 1, 0).getDate();

    let diaAtual = 1;
    let diaProximoMes = 1;

    for (let i = 1; i <= 35; i++) {
        const botaoDia = document.createElement("button");
        botaoDia.type = "button";
        botaoDia.classList.add("dia-marcacoes");

        let dataCompleta;
        let numeroDia;

        if (diaAtual <= totalDiasMes) {
            numeroDia = diaAtual;
            dataCompleta = formatarDataMarcacoes(
                anoAtualMarcacoes,
                mesAtualMarcacoes,
                diaAtual
            );

            diaAtual++;
        } else {
            numeroDia = diaProximoMes;
            dataCompleta = formatarDataMarcacoes(
                anoAtualMarcacoes,
                mesAtualMarcacoes + 1,
                diaProximoMes
            );

            botaoDia.classList.add("dia-outro-mes-marcacoes");
            diaProximoMes++;
        }

        botaoDia.dataset.data = dataCompleta;

        if (diaSelecionadoMarcacoes === dataCompleta) {
            botaoDia.classList.add("dia-selecionado-marcacoes");
        }

        const spanNumero = document.createElement("span");
        spanNumero.classList.add("numero-dia-marcacoes");
        spanNumero.textContent = numeroDia;

        botaoDia.appendChild(spanNumero);

        const marcacoesDoDia = marcacoesMedico.filter(marcacao => {
            return marcacao.data === dataCompleta;
        });

        if (marcacoesDoDia.length > 0) {
            const indicador = document.createElement("span");
            indicador.classList.add("indicador-dia-marcacoes");

            const estadoDoDia = obterEstadoGrupoMarcacoes(marcacoesDoDia);

            if (estadoDoDia === "pendente") {
                indicador.classList.add("pendente");
            } else if (estadoDoDia === "aprovado") {
                indicador.classList.add("aprovado");
            } else {
                indicador.classList.add("recusado");
            }

            indicador.textContent = marcacoesDoDia.length;

            botaoDia.appendChild(indicador);
        }

        botaoDia.addEventListener("click", () => {
            diaSelecionadoMarcacoes = dataCompleta;
            horarioSelecionadoMarcacoes = null;
            pacienteSelecionadoMarcacoes = null;

            renderizarMarcacoesMedico();
        });

        calendario.appendChild(botaoDia);
    }
}

function renderizarHorariosMarcacoes() {
    const listaHorarios = document.getElementById("listaHorariosMarcacoes");

    listaHorarios.innerHTML = "";

    horariosMarcacoes.forEach(hora => {
        const botaoHorario = document.createElement("button");
        botaoHorario.type = "button";
        botaoHorario.classList.add("horario-marcacoes");

        const marcacoesDoHorario = marcacoesMedico.filter(marcacao => {
            return marcacao.data === diaSelecionadoMarcacoes &&
                   marcacao.hora === hora;
        });

        if (marcacoesDoHorario.length > 0) {
            const estadoHorario = obterEstadoGrupoMarcacoes(marcacoesDoHorario);

            if (estadoHorario === "pendente") {
                botaoHorario.classList.add("horario-pendente-marcacoes");
            } else if (estadoHorario === "aprovado") {
                botaoHorario.classList.add("horario-aprovado-marcacoes");
            } else {
                botaoHorario.classList.add("horario-recusado-marcacoes");
            }

            botaoHorario.textContent = `${marcacoesDoHorario.length} Pendente(s)`;
        } else {
            botaoHorario.textContent = "";
        }

        if (horarioSelecionadoMarcacoes === hora) {
            botaoHorario.classList.add("horario-selecionado-marcacoes");
        }

        botaoHorario.addEventListener("click", () => {
            if (!diaSelecionadoMarcacoes) {
                return;
            }

            horarioSelecionadoMarcacoes = hora;
            pacienteSelecionadoMarcacoes = null;

            renderizarHorariosMarcacoes();
            renderizarPacientesMarcacoes();
            renderizarDadosPacienteMarcacoes();
        });

        listaHorarios.appendChild(botaoHorario);
    });
}

function renderizarPacientesMarcacoes() {
    const titulo = document.getElementById("tituloPacientesMarcacoes");
    const listaPacientes = document.getElementById("listaPacientesMarcacoes");

    listaPacientes.innerHTML = "";

    if (!diaSelecionadoMarcacoes) {
        titulo.textContent = "Pacientes";
        listaPacientes.innerHTML = `
            <p class="mensagem-vazia-marcacoes">
                Seleciona um dia no calendário.
            </p>
        `;
        return;
    }

    if (!horarioSelecionadoMarcacoes) {
        titulo.textContent = "Pacientes";
        listaPacientes.innerHTML = `
            <p class="mensagem-vazia-marcacoes">
                Seleciona um horário.
            </p>
        `;
        return;
    }

    const pacientesDoHorario = marcacoesMedico.filter(marcacao => {
        return marcacao.data === diaSelecionadoMarcacoes &&
               marcacao.hora === horarioSelecionadoMarcacoes;
    });

    titulo.textContent = `Pacientes às ${horarioSelecionadoMarcacoes} (${pacientesDoHorario.length})`;

    if (pacientesDoHorario.length === 0) {
        listaPacientes.innerHTML = `
            <p class="mensagem-vazia-marcacoes">
                Não existem pedidos neste horário.
            </p>
        `;
        return;
    }

    pacientesDoHorario.forEach(marcacao => {
        const item = document.createElement("button");
        item.type = "button";
        item.classList.add("paciente-item-marcacoes");

        if (
            pacienteSelecionadoMarcacoes &&
            pacienteSelecionadoMarcacoes.id === marcacao.id
        ) {
            item.classList.add("paciente-selecionado-marcacoes");
        }

        item.innerHTML = `
            <div class="info-paciente-lista-marcacoes">
                <h4>${marcacao.paciente.nome}</h4>
                <p>Solicitado em: ${marcacao.solicitadoEm}</p>
            </div>

            <div>
                <span class="estado-paciente-marcacoes estado-${marcacao.estado}-marcacoes">
                    ${primeiraLetraMaiusculaMarcacoes(marcacao.estado)}
                </span>

                <span class="seta-paciente-marcacoes">›</span>
            </div>
        `;

        item.addEventListener("click", () => {
            pacienteSelecionadoMarcacoes = marcacao;

            renderizarPacientesMarcacoes();
            renderizarDadosPacienteMarcacoes();
        });

        listaPacientes.appendChild(item);
    });
}

function renderizarDadosPacienteMarcacoes() {
    const dadosPaciente = document.getElementById("dadosPacienteMarcacoes");

    if (!pacienteSelecionadoMarcacoes) {
        dadosPaciente.innerHTML = `
            <p class="mensagem-vazia-marcacoes">
                Seleciona um paciente para veres os dados.
            </p>
        `;
        return;
    }

    const marcacao = pacienteSelecionadoMarcacoes;

    const botoesAtivos = marcacao.estado === "pendente";

    dadosPaciente.innerHTML = `
        <div class="perfil-topo-marcacoes">
            <div class="avatar-marcacoes">
                👤
            </div>

            <div>
                <h4>${marcacao.paciente.nome}</h4>
                <p>${marcacao.paciente.idade} anos - ${marcacao.paciente.genero}</p>
                <p>${marcacao.paciente.telefone}</p>
                <p>${marcacao.paciente.email}</p>
            </div>
        </div>

        <div class="linha-dados-marcacoes"></div>

        <div class="detalhes-marcacoes">
            <p><strong>Data:</strong> ${converterDataParaPTMarcacoes(marcacao.data)}</p>
            <p><strong>Hora:</strong> ${marcacao.hora}</p>
            <p><strong>Solicitado em:</strong> ${marcacao.solicitadoEm}</p>
            <p><strong>Motivo:</strong> ${marcacao.motivo}</p>
            <p><strong>Estado:</strong> ${primeiraLetraMaiusculaMarcacoes(marcacao.estado)}</p>
        </div>

        <div class="acoes-marcacoes">
            <button 
                class="btn-acao-marcacoes btn-aceitar-marcacoes"
                type="button"
                ${botoesAtivos ? "" : "disabled"}
                onclick="aceitarMarcacaoMedico(${marcacao.id})"
            >
                Aceitar
            </button>

            <button 
                class="btn-acao-marcacoes btn-recusar-marcacoes"
                type="button"
                ${botoesAtivos ? "" : "disabled"}
                onclick="recusarMarcacaoMedico(${marcacao.id})"
            >
                Recusar
            </button>
        </div>
    `;
}

function aceitarMarcacaoMedico(idMarcacao) {
    const marcacaoAceite = marcacoesMedico.find(marcacao => {
        return marcacao.id === idMarcacao;
    });

    if (!marcacaoAceite) {
        return;
    }

    marcacoesMedico.forEach(marcacao => {
        const mesmaData = marcacao.data === marcacaoAceite.data;
        const mesmaHora = marcacao.hora === marcacaoAceite.hora;

        if (mesmaData && mesmaHora) {
            if (marcacao.id === idMarcacao) {
                marcacao.estado = "aprovado";
            } else {
                marcacao.estado = "recusado";
            }
        }
    });

    pacienteSelecionadoMarcacoes = marcacoesMedico.find(marcacao => {
        return marcacao.id === idMarcacao;
    });

    guardarMarcacoesMedico();
    renderizarMarcacoesMedico();
}

function recusarMarcacaoMedico(idMarcacao) {
    const marcacao = marcacoesMedico.find(marcacao => {
        return marcacao.id === idMarcacao;
    });

    if (!marcacao) {
        return;
    }

    marcacao.estado = "recusado";
    pacienteSelecionadoMarcacoes = marcacao;

    guardarMarcacoesMedico();
    renderizarMarcacoesMedico();
}

function obterEstadoGrupoMarcacoes(listaMarcacoes) {
    const existePendente = listaMarcacoes.some(marcacao => {
        return marcacao.estado === "pendente";
    });

    const existeAprovado = listaMarcacoes.some(marcacao => {
        return marcacao.estado === "aprovado";
    });

    if (existePendente) {
        return "pendente";
    }

    if (existeAprovado) {
        return "aprovado";
    }

    return "recusado";
}

function voltarMesMarcacoes() {
    mesAtualMarcacoes--;

    if (mesAtualMarcacoes < 0) {
        mesAtualMarcacoes = 11;
        anoAtualMarcacoes--;
    }

    diaSelecionadoMarcacoes = null;
    horarioSelecionadoMarcacoes = null;
    pacienteSelecionadoMarcacoes = null;

    renderizarMarcacoesMedico();
}

function avancarMesMarcacoes() {
    mesAtualMarcacoes++;

    if (mesAtualMarcacoes > 11) {
        mesAtualMarcacoes = 0;
        anoAtualMarcacoes++;
    }

    diaSelecionadoMarcacoes = null;
    horarioSelecionadoMarcacoes = null;
    pacienteSelecionadoMarcacoes = null;

    renderizarMarcacoesMedico();
}

function formatarDataMarcacoes(ano, mes, dia) {
    const mesFormatado = String(mes + 1).padStart(2, "0");
    const diaFormatado = String(dia).padStart(2, "0");

    return `${ano}-${mesFormatado}-${diaFormatado}`;
}

function converterDataParaPTMarcacoes(data) {
    const partes = data.split("-");

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function primeiraLetraMaiusculaMarcacoes(texto) {
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}