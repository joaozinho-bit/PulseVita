// ===== Dados Médicos: histórico + filtro (modal) + ordenação + paginação =====
// Filtros e ordenação são aplicados pelo backend; o cliente só pagina.

const ITENS_POR_PAGINA = 4;
let paginaAtual = 1;

// Filtro só é aplicado quando o utilizador clica em "Aplicar"
let filtroAtivo = { inicio: "", fim: "", tipo: "" };

let historico = [];

const NOMES_TIPO = {
    TEMPERATURA: "Temperatura",
    BPM: "Frequência cardíaca",
    AMBOS: "Medição completa"
};

const NOMES_AVALIACAO = {
    NORMAL: "Normal",
    FEBRE: "Febre",
    BPM_ALTO: "BPM elevado",
    BPM_BAIXO: "BPM baixo"
};

function formatarData(isoString) {
    const d = new Date(isoString);
    const dia = String(d.getDate()).padStart(2, "0");
    const mes = String(d.getMonth() + 1).padStart(2, "0");
    const ano = d.getFullYear();
    const horas = String(d.getHours()).padStart(2, "0");
    const minutos = String(d.getMinutes()).padStart(2, "0");
    return `${dia}/${mes}/${ano} - ${horas}:${minutos}`;
}

function formatarTemperatura(valor) {
    return valor.toFixed(1).replace(".", ",");
}

// a avaliacao vem do backend ja calculada no momento da medicao
function formatarAvaliacao(avaliacao) {
    if (!avaliacao) return "";
    return avaliacao.split(",").map(a => NOMES_AVALIACAO[a] || a).join(" + ");
}

async function carregarHistorico() {
    const ordem = document.getElementById("ordenar-por").value;
    const params = new URLSearchParams();
    if (filtroAtivo.tipo) params.set("tipo", filtroAtivo.tipo);
    if (filtroAtivo.inicio) params.set("dataInicio", filtroAtivo.inicio);
    if (filtroAtivo.fim) params.set("dataFim", filtroAtivo.fim);
    params.set("ordem", ordem);

    const resposta = await fetch(`${API_BASE_URL}/historico?${params}`, { credentials: "include" });
    if (resposta.status === 401) {
        window.location.href = "../index.html";
        return [];
    }
    if (!resposta.ok) throw new Error();
    return resposta.json();
}

// cartoes do topo: ultimo valor conhecido de cada grandeza, seja de que
// tipo de medicao for; recebem a primeira carga, sem filtros e por ordem recente
function renderUltimaMedicao(lista) {
    const comBpm = lista.find(item => item.bpm != null);
    const comTemp = lista.find(item => item.temperatura != null);
    document.getElementById("ultimo-bpm").textContent =
        comBpm ? `${comBpm.bpm} bpm` : "-- bpm";
    document.getElementById("ultima-temp").textContent =
        comTemp ? `${formatarTemperatura(comTemp.temperatura)} °C` : "-- °C";
}

function renderHistorico(lista, pagina) {
    const container = document.getElementById("lista-historico");
    container.innerHTML = "";

    if (lista.length === 0) {
        container.innerHTML = `<p class="sem-resultados">Nenhuma medição encontrada para o período selecionado.</p>`;
        return;
    }

    const inicio = (pagina - 1) * ITENS_POR_PAGINA;
    const itensPagina = lista.slice(inicio, inicio + ITENS_POR_PAGINA);

    itensPagina.forEach(item => {
        // cada registo mostra apenas os valores que o seu tipo de medicao tem
        const linhas = [];
        if (item.bpm != null) {
            linhas.push(`<p>Batimentos cardíacos: <span>${item.bpm} bpm</span></p>`);
        }
        if (item.temperatura != null) {
            linhas.push(`<p>Temperatura corporal: <span>${formatarTemperatura(item.temperatura)} °C</span></p>`);
        }

        const textoAvaliacao = formatarAvaliacao(item.avaliacao);
        const badgeAvaliacao = textoAvaliacao
            ? `<p class="avaliacao ${item.avaliacao === "NORMAL" ? "" : "alerta"}">${textoAvaliacao}</p>`
            : "";

        const box = document.createElement("div");
        box.className = "box-info";
        box.innerHTML = `
            <div class="box-dados">
                <p class="tipo-medicao">${NOMES_TIPO[item.tipoMedicao] || "Medição"}</p>
                ${linhas.join("")}
                ${badgeAvaliacao}
                <p class="data">${formatarData(item.dataLeitura)}</p>
            </div>
        `;
        container.appendChild(box);
    });
}

function renderPaginacao(lista) {
    const totalPaginas = Math.max(1, Math.ceil(lista.length / ITENS_POR_PAGINA));
    const nav = document.getElementById("paginacao");
    nav.innerHTML = "";

    const btnAnterior = document.createElement("button");
    btnAnterior.innerHTML = `<i class="bi bi-chevron-left"></i>`;
    btnAnterior.disabled = paginaAtual === 1;
    btnAnterior.addEventListener("click", () => irParaPagina(paginaAtual - 1));
    nav.appendChild(btnAnterior);

    for (let i = 1; i <= totalPaginas; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;
        if (i === paginaAtual) btn.classList.add("ativo");
        btn.addEventListener("click", () => irParaPagina(i));
        nav.appendChild(btn);
    }

    const btnProximo = document.createElement("button");
    btnProximo.innerHTML = `<i class="bi bi-chevron-right"></i>`;
    btnProximo.disabled = paginaAtual === totalPaginas;
    btnProximo.addEventListener("click", () => irParaPagina(paginaAtual + 1));
    nav.appendChild(btnProximo);
}

function renderizar() {
    renderHistorico(historico, paginaAtual);
    renderPaginacao(historico);
}

// mudar de pagina nao volta ao servidor; so filtros e ordenacao recarregam
async function atualizarHistorico() {
    try {
        historico = await carregarHistorico();
    } catch (erro) {
        document.getElementById("lista-historico").innerHTML =
            `<p class="sem-resultados">Não foi possível carregar o histórico. Tente novamente mais tarde.</p>`;
        document.getElementById("paginacao").innerHTML = "";
        return;
    }
    renderizar();
}

function irParaPagina(pagina) {
    paginaAtual = pagina;
    renderizar();
}

// ===== Modal de filtro =====
function abrirModalFiltro() {
    document.getElementById("modal-tipo").value = filtroAtivo.tipo;
    document.getElementById("modal-data-inicio").value = filtroAtivo.inicio;
    document.getElementById("modal-data-fim").value = filtroAtivo.fim;
    document.getElementById("modalFiltroOverlay").classList.add("aberto");
}

function fecharModalFiltro() {
    document.getElementById("modalFiltroOverlay").classList.remove("aberto");
}

function aplicarFiltro() {
    filtroAtivo.tipo = document.getElementById("modal-tipo").value;
    filtroAtivo.inicio = document.getElementById("modal-data-inicio").value;
    filtroAtivo.fim = document.getElementById("modal-data-fim").value;

    document.getElementById("btn-abrir-filtro")
        .classList.toggle("ativo", !!(filtroAtivo.tipo || filtroAtivo.inicio || filtroAtivo.fim));

    paginaAtual = 1;
    atualizarHistorico();
    fecharModalFiltro();
}

function limparFiltro() {
    filtroAtivo = { inicio: "", fim: "", tipo: "" };
    document.getElementById("modal-tipo").value = "";
    document.getElementById("modal-data-inicio").value = "";
    document.getElementById("modal-data-fim").value = "";
    document.getElementById("btn-abrir-filtro").classList.remove("ativo");

    paginaAtual = 1;
    atualizarHistorico();
    fecharModalFiltro();
}

document.addEventListener("DOMContentLoaded", async () => {
    await atualizarHistorico();
    renderUltimaMedicao(historico);

    document.getElementById("ordenar-por").addEventListener("change", () => {
        paginaAtual = 1;
        atualizarHistorico();
    });

    document.getElementById("btn-abrir-filtro").addEventListener("click", abrirModalFiltro);
    document.getElementById("btnFecharFiltro").addEventListener("click", fecharModalFiltro);
    document.getElementById("btnAplicarFiltro").addEventListener("click", aplicarFiltro);
    document.getElementById("btnLimparFiltro").addEventListener("click", limparFiltro);

    document.getElementById("modalFiltroOverlay").addEventListener("click", (e) => {
        if (e.target.id === "modalFiltroOverlay") fecharModalFiltro();
    });
});
