// ===== Dados Médicos: histórico + paginação =====
// TODO: substituir HISTORICO_MOCK por fetch('http://localhost:8080/medicoes/historico')

const ITENS_POR_PAGINA = 4;
let paginaAtual = 1;

const HISTORICO_MOCK = [
    { bpm: 96, temp: "37,6", data: "18/07/2026 - 12:55" },
    { bpm: 96, temp: "37,6", data: "18/07/2026 - 12:55" },
    { bpm: 96, temp: "37,6", data: "18/07/2026 - 12:55" },
    { bpm: 96, temp: "37,6", data: "18/07/2026 - 12:55" },
    { bpm: 98, temp: "37,4", data: "17/07/2026 - 09:20" },
    { bpm: 91, temp: "37,1", data: "16/07/2026 - 21:10" },
    { bpm: 95, temp: "37,3", data: "16/07/2026 - 08:45" },
    { bpm: 93, temp: "37,0", data: "15/07/2026 - 19:30" },
    { bpm: 97, temp: "37,5", data: "15/07/2026 - 07:15" },
];

function renderUltimaMedicao(medicao) {
    document.getElementById("ultimo-bpm").textContent = `${medicao.bpm} bpm`;
    document.getElementById("ultima-temp").textContent = `${medicao.temp} °C`;
}

function renderHistorico(pagina) {
    const container = document.getElementById("lista-historico");
    container.innerHTML = "";

    const inicio = (pagina - 1) * ITENS_POR_PAGINA;
    const itensPagina = HISTORICO_MOCK.slice(inicio, inicio + ITENS_POR_PAGINA);

    itensPagina.forEach(item => {
        const box = document.createElement("div");
        box.className = "box-info";
        box.innerHTML = `
            <div class="box-dados">
                <p>Batimentos cardíacos: <span>${item.bpm} bpm</span></p>
                <p>Temperatura corporal: <span>${item.temp} °C</span></p>
                <p class="data">${item.data}</p>
            </div>
        `;
        container.appendChild(box);
    });
}

function renderPaginacao() {
    const totalPaginas = Math.ceil(HISTORICO_MOCK.length / ITENS_POR_PAGINA);
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

function irParaPagina(pagina) {
    paginaAtual = pagina;
    renderHistorico(paginaAtual);
    renderPaginacao();
}

document.addEventListener("DOMContentLoaded", () => {
    renderUltimaMedicao(HISTORICO_MOCK[0]);
    renderHistorico(paginaAtual);
    renderPaginacao();
});