// ===== Dados Médicos: histórico + filtro (modal) + ordenação + paginação =====
// TODO: substituir HISTORICO_MOCK por fetch('http://localhost:8080/medicoes/historico')

const ITENS_POR_PAGINA = 4;
let paginaAtual = 1;

// Filtro só é aplicado quando o utilizador clica em "Aplicar"
let filtroAtivo = { inicio: "", fim: "" };

const HISTORICO_MOCK = [
    { bpm: 96, temp: "37,6", data: "2026-07-18T12:55" },
    { bpm: 96, temp: "37,6", data: "2026-07-18T12:55" },
    { bpm: 96, temp: "37,6", data: "2026-07-18T12:55" },
    { bpm: 96, temp: "37,6", data: "2026-07-18T12:55" },
    { bpm: 98, temp: "37,4", data: "2026-07-17T09:20" },
    { bpm: 91, temp: "37,1", data: "2026-07-16T21:10" },
    { bpm: 95, temp: "37,3", data: "2026-07-16T08:45" },
    { bpm: 93, temp: "37,0", data: "2026-07-15T19:30" },
    { bpm: 97, temp: "37,5", data: "2026-07-15T07:15" },
];

function formatarData(isoString) {
    const d = new Date(isoString);
    const dia = String(d.getDate()).padStart(2, "0");
    const mes = String(d.getMonth() + 1).padStart(2, "0");
    const ano = d.getFullYear();
    const horas = String(d.getHours()).padStart(2, "0");
    const minutos = String(d.getMinutes()).padStart(2, "0");
    return `${dia}/${mes}/${ano} - ${horas}:${minutos}`;
}

function obterListaProcessada() {
    const ordem = document.getElementById("ordenar-por").value;
    let lista = [...HISTORICO_MOCK];

    if (filtroAtivo.inicio) {
        lista = lista.filter(item => item.data.slice(0, 10) >= filtroAtivo.inicio);
    }
    if (filtroAtivo.fim) {
        lista = lista.filter(item => item.data.slice(0, 10) <= filtroAtivo.fim);
    }

    lista.sort((a, b) => {
        return ordem === "antigas"
            ? new Date(a.data) - new Date(b.data)
            : new Date(b.data) - new Date(a.data);
    });

    return lista;
}

function renderUltimaMedicao(medicao) {
    document.getElementById("ultimo-bpm").textContent = `${medicao.bpm} bpm`;
    document.getElementById("ultima-temp").textContent = `${medicao.temp} °C`;
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
        const box = document.createElement("div");
        box.className = "box-info";
        box.innerHTML = `
            <div class="box-dados">
                <p>Batimentos cardíacos: <span>${item.bpm} bpm</span></p>
                <p>Temperatura corporal: <span>${item.temp} °C</span></p>
                <p class="data">${formatarData(item.data)}</p>
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

function atualizarHistorico() {
    const lista = obterListaProcessada();
    renderHistorico(lista, paginaAtual);
    renderPaginacao(lista);
}

function irParaPagina(pagina) {
    paginaAtual = pagina;
    atualizarHistorico();
}

// ===== Modal de filtro =====
function abrirModalFiltro() {
    document.getElementById("modal-data-inicio").value = filtroAtivo.inicio;
    document.getElementById("modal-data-fim").value = filtroAtivo.fim;
    document.getElementById("modalFiltroOverlay").classList.add("aberto");
}

function fecharModalFiltro() {
    document.getElementById("modalFiltroOverlay").classList.remove("aberto");
}

function aplicarFiltro() {
    filtroAtivo.inicio = document.getElementById("modal-data-inicio").value;
    filtroAtivo.fim = document.getElementById("modal-data-fim").value;

    document.getElementById("btn-abrir-filtro")
        .classList.toggle("ativo", !!(filtroAtivo.inicio || filtroAtivo.fim));

    paginaAtual = 1;
    atualizarHistorico();
    fecharModalFiltro();
}

function limparFiltro() {
    filtroAtivo = { inicio: "", fim: "" };
    document.getElementById("modal-data-inicio").value = "";
    document.getElementById("modal-data-fim").value = "";
    document.getElementById("btn-abrir-filtro").classList.remove("ativo");

    paginaAtual = 1;
    atualizarHistorico();
    fecharModalFiltro();
}

document.addEventListener("DOMContentLoaded", () => {
    renderUltimaMedicao(HISTORICO_MOCK[0]);
    atualizarHistorico();

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