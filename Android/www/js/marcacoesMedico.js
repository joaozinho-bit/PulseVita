const API_BASE_URL = "http://localhost:8080";

let mesAtualMarcacoes = new Date().getMonth();
let anoAtualMarcacoes = new Date().getFullYear();

let diaSelecionadoMarcacoes = null;
let horarioSelecionadoMarcacoes = null;
let pacienteSelecionadoMarcacoes = null;

const horariosMarcacoes = ["09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00"];
const nomesMesesMarcacoes = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"];

let marcacoesMedico = [];

async function carregarMarcacoesMedico() {
    try {
        const resposta = await fetch(
            `${API_BASE_URL}/consultas/medico/mes?ano=${anoAtualMarcacoes}&mes=${mesAtualMarcacoes + 1}`
        );
        if (!resposta.ok) throw new Error("Erro ao carregar consultas");
        marcacoesMedico = await resposta.json();
    } catch (erro) {
        console.error("Erro ao carregar marcações:", erro);
        marcacoesMedico = [];
    }
}

async function iniciarMarcacoesMedico() {
    const calendario = document.getElementById("calendarioMarcacoes");
    if (!calendario) return;

    document.getElementById("btnMesAnteriorMarcacoes").addEventListener("click", voltarMesMarcacoes);
    document.getElementById("btnMesSeguinteMarcacoes").addEventListener("click", avancarMesMarcacoes);

    await carregarMarcacoesMedico();
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

    // Dia da semana em que cai o dia 1 (0 = domingo ... 6 = sábado)
    const diaSemanaInicio = new Date(anoAtualMarcacoes, mesAtualMarcacoes, 1).getDay();

    // Total de dias do mês anterior, para preencher as casinhas iniciais
    const totalDiasMesAnterior = new Date(anoAtualMarcacoes, mesAtualMarcacoes, 0).getDate();

    const totalCasas = 42; // 6 semanas fixas, garante sempre espaço suficiente
    let diaMesAnterior = totalDiasMesAnterior - diaSemanaInicio + 1;
    let diaAtual = 1;
    let diaProximoMes = 1;

    for (let i = 0; i < totalCasas; i++) {
        const botaoDia = document.createElement("button");
        botaoDia.type = "button";
        botaoDia.classList.add("dia-marcacoes");

        let dataCompleta, numeroDia;

        if (i < diaSemanaInicio) {
            // Casinhas antes do dia 1 -> dias do mês anterior
            numeroDia = diaMesAnterior;
            const mesAnterior = mesAtualMarcacoes === 0 ? 11 : mesAtualMarcacoes - 1;
            const anoDoMesAnterior = mesAtualMarcacoes === 0 ? anoAtualMarcacoes - 1 : anoAtualMarcacoes;
            dataCompleta = formatarDataMarcacoes(anoDoMesAnterior, mesAnterior, diaMesAnterior);
            botaoDia.classList.add("dia-outro-mes-marcacoes");
            diaMesAnterior++;
        } else if (diaAtual <= totalDiasMes) {
            numeroDia = diaAtual;
            dataCompleta = formatarDataMarcacoes(anoAtualMarcacoes, mesAtualMarcacoes, diaAtual);
            diaAtual++;
        } else {
            numeroDia = diaProximoMes;
            const mesSeguinte = mesAtualMarcacoes === 11 ? 0 : mesAtualMarcacoes + 1;
            const anoDoMesSeguinte = mesAtualMarcacoes === 11 ? anoAtualMarcacoes + 1 : anoAtualMarcacoes;
            dataCompleta = formatarDataMarcacoes(anoDoMesSeguinte, mesSeguinte, diaProximoMes);
            botaoDia.classList.add("dia-outro-mes-marcacoes");
            diaProximoMes++;
        }

        botaoDia.dataset.data = dataCompleta;
        if (diaSelecionadoMarcacoes === dataCompleta) botaoDia.classList.add("dia-selecionado-marcacoes");

        const spanNumero = document.createElement("span");
        spanNumero.classList.add("numero-dia-marcacoes");
        spanNumero.textContent = numeroDia;
        botaoDia.appendChild(spanNumero);

        const marcacoesDoDia = marcacoesMedico.filter(m => m.data === dataCompleta);
        if (marcacoesDoDia.length > 0) {
            const indicador = document.createElement("span");
            indicador.classList.add("indicador-dia-marcacoes");
            const estadoDoDia = obterEstadoGrupoMarcacoes(marcacoesDoDia);
            indicador.classList.add(estadoDoDia === "pendente" ? "pendente" : estadoDoDia === "aprovado" ? "aprovado" : "recusado");
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

        const marcacoesDoHorario = marcacoesMedico.filter(
            m => m.data === diaSelecionadoMarcacoes &&
                 obterBlocoHoraMarcacoes(m.hora) === hora
        );

        if (marcacoesDoHorario.length > 0) {
            const estadoHorario = obterEstadoGrupoMarcacoes(marcacoesDoHorario);
            botaoHorario.classList.add(
                estadoHorario === "pendente" ? "horario-pendente-marcacoes"
                : estadoHorario === "aprovado" ? "horario-aprovado-marcacoes"
                : "horario-recusado-marcacoes"
            );
            botaoHorario.textContent = `${marcacoesDoHorario.length} Pedido(s)`;
        } else {
            botaoHorario.textContent = "";
        }

        if (horarioSelecionadoMarcacoes === hora) botaoHorario.classList.add("horario-selecionado-marcacoes");

        botaoHorario.addEventListener("click", () => {
            if (!diaSelecionadoMarcacoes) return;
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
        listaPacientes.innerHTML = `<p class="mensagem-vazia-marcacoes">Seleciona um dia no calendário.</p>`;
        return;
    }
    if (!horarioSelecionadoMarcacoes) {
        titulo.textContent = "Pacientes";
        listaPacientes.innerHTML = `<p class="mensagem-vazia-marcacoes">Seleciona um horário.</p>`;
        return;
    }

    const pacientesDoHorario = marcacoesMedico.filter(
        m => m.data === diaSelecionadoMarcacoes &&
             obterBlocoHoraMarcacoes(m.hora) === horarioSelecionadoMarcacoes
    );

    titulo.textContent = `Pacientes às ${horarioSelecionadoMarcacoes} (${pacientesDoHorario.length})`;

    if (pacientesDoHorario.length === 0) {
        listaPacientes.innerHTML = `<p class="mensagem-vazia-marcacoes">Não existem pedidos neste horário.</p>`;
        return;
    }

    pacientesDoHorario.forEach(marcacao => {
        const item = document.createElement("button");
        item.type = "button";
        item.classList.add("paciente-item-marcacoes");
        if (pacienteSelecionadoMarcacoes && pacienteSelecionadoMarcacoes.id === marcacao.id) {
            item.classList.add("paciente-selecionado-marcacoes");
        }

        item.innerHTML = `
            <div class="info-paciente-lista-marcacoes">
                <h4>${marcacao.paciente ? marcacao.paciente.nome : "Paciente"}</h4>
                <p>Pedido para as ${marcacao.hora}</p>
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
        dadosPaciente.innerHTML = `<p class="mensagem-vazia-marcacoes">Seleciona um paciente para veres os dados.</p>`;
        return;
    }

    const marcacao = pacienteSelecionadoMarcacoes;
    const p = marcacao.paciente || {};
    const botoesAtivos = marcacao.estado === "pendente";

    dadosPaciente.innerHTML = `
        <div class="perfil-topo-marcacoes">
            <div class="avatar-marcacoes">👤</div>
            <div>
                <h4>${p.nome || "N/A"}</h4>
                <p>${p.idade ?? "?"} anos - ${p.genero || "N/A"}</p>
                <p>${p.telefone || "N/A"}</p>
                <p>${p.email || "N/A"}</p>
            </div>
        </div>
        <div class="linha-dados-marcacoes"></div>
        <div class="detalhes-marcacoes">
            <p><strong>Data:</strong> ${converterDataParaPTMarcacoes(marcacao.data)}</p>
            <p><strong>Hora:</strong> ${marcacao.hora}</p>
            <p><strong>Motivo:</strong> ${marcacao.motivo || "Não especificado"}</p>
            <p><strong>Estado:</strong> ${primeiraLetraMaiusculaMarcacoes(marcacao.estado)}</p>
        </div>
        <div class="acoes-marcacoes">
            <button class="btn-acao-marcacoes btn-aceitar-marcacoes" type="button"
                ${botoesAtivos ? "" : "disabled"} onclick="aceitarMarcacaoMedico(${marcacao.id})">Aceitar</button>
            <button class="btn-acao-marcacoes btn-recusar-marcacoes" type="button"
                ${botoesAtivos ? "" : "disabled"} onclick="recusarMarcacaoMedico(${marcacao.id})">Recusar</button>
        </div>
    `;
}

async function aceitarMarcacaoMedico(idMarcacao) {
    try {
        const resposta = await fetch(`${API_BASE_URL}/consultas/${idMarcacao}/confirmar`, {
            method: "POST",
            credentials: "include"
        });
        if (!resposta.ok) { alert("Não foi possível confirmar a consulta."); return; }
        await carregarMarcacoesMedico();
        pacienteSelecionadoMarcacoes = marcacoesMedico.find(m => m.id === idMarcacao) || null;
        renderizarMarcacoesMedico();
    } catch (erro) {
        alert("Não foi possível contactar o servidor.");
    }
}

async function recusarMarcacaoMedico(idMarcacao) {
    try {
        const resposta = await fetch(`${API_BASE_URL}/consultas/${idMarcacao}/recusar`, { method: "POST" });
        if (!resposta.ok) { alert("Não foi possível recusar a consulta."); return; }
        await carregarMarcacoesMedico();
        pacienteSelecionadoMarcacoes = marcacoesMedico.find(m => m.id === idMarcacao) || null;
        renderizarMarcacoesMedico();
    } catch (erro) {
        alert("Não foi possível contactar o servidor.");
    }
}

function obterEstadoGrupoMarcacoes(lista) {
    if (lista.some(m => m.estado === "pendente")) return "pendente";
    if (lista.some(m => m.estado === "aprovado")) return "aprovado";
    return "recusado";
}

async function voltarMesMarcacoes() {
    mesAtualMarcacoes--;
    if (mesAtualMarcacoes < 0) { mesAtualMarcacoes = 11; anoAtualMarcacoes--; }
    diaSelecionadoMarcacoes = null; horarioSelecionadoMarcacoes = null; pacienteSelecionadoMarcacoes = null;
    await carregarMarcacoesMedico();
    renderizarMarcacoesMedico();
}

async function avancarMesMarcacoes() {
    mesAtualMarcacoes++;
    if (mesAtualMarcacoes > 11) { mesAtualMarcacoes = 0; anoAtualMarcacoes++; }
    diaSelecionadoMarcacoes = null; horarioSelecionadoMarcacoes = null; pacienteSelecionadoMarcacoes = null;
    await carregarMarcacoesMedico();
    renderizarMarcacoesMedico();
}

function formatarDataMarcacoes(ano, mes, dia) {
    return `${ano}-${String(mes + 1).padStart(2, "0")}-${String(dia).padStart(2, "0")}`;
}

function converterDataParaPTMarcacoes(data) {
    const [a, m, d] = data.split("-");
    return `${d}/${m}/${a}`;
}

function primeiraLetraMaiusculaMarcacoes(texto) {
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}
function obterBlocoHoraMarcacoes(horaCompleta) {
    if (!horaCompleta) return null;
    const [h] = horaCompleta.split(":");
    return `${h.padStart(2, "0")}:00`;
}