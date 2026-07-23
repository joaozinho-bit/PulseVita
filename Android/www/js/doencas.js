// Separadores (Medições / Doenças) do ecra Dados Medicos.
// As medicoes continuam a ser geridas por dadosmedicos.js; aqui trata-se apenas
// da troca de separador e do carregamento e filtragem das doencas.
(function () {
    const MESES = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];

    let doencas = null;   // null enquanto ainda nao foram carregadas do backend
    let filtroEstado = "";

    function formatarData(iso) {
        const [a, m, d] = iso.split("-").map(Number);
        return `${String(d).padStart(2, "0")} ${MESES[m - 1]} ${a}`;
    }

    // icone escolhido pela area clinica, para leitura mais rapida
    function iconeDoenca(nome) {
        const n = (nome || "").toLowerCase();
        if (n.includes("pneumonia") || n.includes("gripe")) return "bi-lungs";
        if (n.includes("cardia") || n.includes("tensão") || n.includes("hipertensão") || n.includes("arritmia")) return "bi-heart-pulse";
        return "bi-clipboard2-pulse";
    }

    function cartao(d) {
        const terminada = d.estado === "TERMINADA";
        const badge = terminada
            ? `<span class="doenca-badge terminada"><i class="bi bi-check2"></i> Terminada</span>`
            : `<span class="doenca-badge ativa"><i class="bi bi-circle-fill" style="font-size:0.5rem"></i> Ativa</span>`;
        const cronica = d.cronica
            ? `<span class="doenca-cronica"><i class="bi bi-arrow-repeat"></i> Crónica</span>` : "";
        const diag = d.dataDiagnostico
            ? `<span><i class="bi bi-calendar-event"></i> Diagnóstico: ${formatarData(d.dataDiagnostico)}</span>` : "";
        const fim = d.dataFim
            ? `<span><i class="bi bi-calendar-check"></i> Terminada em ${formatarData(d.dataFim)}</span>` : "";
        const medico = d.medico
            ? `<span><i class="bi bi-person-badge"></i> ${d.medico}</span>` : "";
        const obs = d.observacoes
            ? `<p class="doenca-obs">${d.observacoes}</p>` : "";

        return `
            <article class="cartao-doenca ${terminada ? "terminada" : ""}">
                <div class="doenca-icone"><i class="bi ${iconeDoenca(d.nome)}"></i></div>
                <div class="doenca-corpo">
                    <div class="doenca-topo">
                        <h2>${d.nome || "Doença"}</h2>
                        ${badge}
                    </div>
                    ${cronica}
                    <div class="doenca-meta">
                        ${diag}
                        ${fim}
                        ${medico}
                    </div>
                    ${obs}
                </div>
            </article>`;
    }

    function render() {
        const cont = document.getElementById("lista-doencas");
        if (!Array.isArray(doencas)) return;

        const lista = filtroEstado ? doencas.filter(d => d.estado === filtroEstado) : doencas;
        if (lista.length === 0) {
            const msg = doencas.length === 0
                ? "Não tem doenças registadas."
                : "Nenhuma doença corresponde ao filtro selecionado.";
            cont.innerHTML = `<div class="doencas-vazio"><i class="bi bi-clipboard2-check"></i><p>${msg}</p></div>`;
            return;
        }
        cont.innerHTML = lista.map(cartao).join("");
    }

    async function carregar() {
        const cont = document.getElementById("lista-doencas");
        cont.innerHTML = `<div class="doencas-vazio"><i class="bi bi-hourglass-split"></i><p>A carregar…</p></div>`;
        try {
            const resp = await fetch(`${API_BASE_URL}/doencas/minhas`, { credentials: "include" });
            if (resp.status === 401) { window.location.href = "../index.html"; return; }
            if (!resp.ok) throw new Error();
            doencas = await resp.json();
        } catch (erro) {
            doencas = null;
            cont.innerHTML = `<div class="doencas-vazio"><i class="bi bi-wifi-off"></i><p>Não foi possível carregar as doenças.</p></div>`;
            return;
        }
        render();
    }

    function mostrarPainel(nome, botao) {
        document.querySelectorAll(".tab").forEach(t => t.classList.remove("ativo"));
        botao.classList.add("ativo");
        document.querySelectorAll(".painel").forEach(p => p.classList.remove("ativo"));
        document.getElementById("painel-" + nome).classList.add("ativo");

        // as doencas so sao pedidas ao backend na primeira vez que a tab e aberta
        if (nome === "doencas" && doencas === null) carregar();
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll(".tab").forEach(tab => {
            tab.addEventListener("click", () => mostrarPainel(tab.dataset.painel, tab));
        });

        document.getElementById("doencas-chips").addEventListener("click", (e) => {
            const btn = e.target.closest(".doenca-chip");
            if (!btn) return;
            document.querySelectorAll(".doenca-chip").forEach(c => c.classList.remove("ativo"));
            btn.classList.add("ativo");
            filtroEstado = btn.dataset.estado;
            render();
        });
    });
})();
