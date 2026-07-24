// Dashboard do paciente: um unico pedido /dashboard traz utilizador, proxima
// consulta e ultima medicao. O ecra decide automaticamente o cartao de estado
// diario (sem medicao / medicao normal / medicao com alerta).
(function () {
    const MESES = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];
    const DIAS = ["domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado"];

    // rotulos de apresentacao da avaliacao (mesma linguagem do ecra de medicoes)
    const NOMES_AVALIACAO = { NORMAL: "Normal", FEBRE: "Febre", BPM_ALTO: "BPM elevado", BPM_BAIXO: "BPM baixo" };
    const NOMES_ESTADO = { CONFIRMADA: "Confirmada", CONCLUIDA: "Concluída", POR_CONFIRMAR: "Por confirmar", CANCELADA: "Cancelada" };

    function formatarAvaliacao(a) {
        if (!a) return "";
        return a.split(",").map(x => NOMES_AVALIACAO[x] || x).join(" + ");
    }
    const avaliacaoNormal = a => a === "NORMAL";

    const fmtTemp = t => Number(t).toFixed(1).replace(".", ",");
    function dataLonga(iso) {
        const [a, m, d] = iso.split("-").map(Number);
        return `${d} ${MESES[m - 1]} ${a}`;
    }
    function hojeExtenso() {
        const d = new Date();
        const s = `${DIAS[d.getDay()]}, ${d.getDate()} ${MESES[d.getMonth()]}`;
        return s.charAt(0).toUpperCase() + s.slice(1);
    }

    // ---- Blocos de HTML ----
    function cabecalho(u) {
        return `
            <div class="dash-user dash-anim">
                <img class="dash-user__foto" id="dash-foto" src="../Imagens/user.png" alt="Foto de perfil">
                <div class="dash-user__txt">
                    <div class="dash-user__ola">Olá,</div>
                    <div class="dash-user__nome">${u.nome || "Paciente"}</div>
                    <div class="dash-user__num"><i class="bi bi-person-vcard"></i> Nº paciente ${u.nPaciente || "—"}</div>
                </div>
                <div class="dash-user__data">${hojeExtenso()}</div>
            </div>`;
    }

    function tilesMedicao(m, contexto) {
        // contexto "hero" usa tiles translucidos; "card" usa tiles claros
        const cls = contexto === "hero" ? "dash-valor" : "dash-tile";
        const lbl = contexto === "hero" ? "dash-valor__lbl" : "dash-tile__lbl";
        const num = contexto === "hero" ? "dash-valor__num" : "dash-tile__num";
        const partes = [];
        if (m.temperatura != null) {
            partes.push(`<div class="${cls}"><span class="${lbl}"><i class="bi bi-thermometer-half"></i> Temperatura</span><span class="${num}">${fmtTemp(m.temperatura)}<small> °C</small></span></div>`);
        }
        if (m.bpm != null) {
            partes.push(`<div class="${cls}"><span class="${lbl}"><i class="bi bi-heart-pulse"></i> Frequência</span><span class="${num}">${m.bpm}<small> bpm</small></span></div>`);
        }
        const wrap = contexto === "hero" ? "dash-valores" : "dash-tiles";
        return partes.length ? `<div class="${wrap}">${partes.join("")}</div>` : "";
    }

    function heroSemMedicao() {
        return `
            <article class="dash-hero dash-hero--aviso dash-anim" style="animation-delay:.05s">
                <span class="dash-hoje"><i class="bi bi-exclamation-circle-fill"></i> Ação de hoje</span>
                <div class="dash-hero__top">
                    <div class="dash-hero__icone"><i class="bi bi-clipboard2-pulse"></i></div>
                    <div>
                        <div class="dash-hero__titulo">Ainda não realizou nenhuma medição hoje.</div>
                        <div class="dash-hero__sub">Realize a medição diária para acompanhar o seu estado de saúde.</div>
                    </div>
                </div>
                <div class="dash-acoes">
                    <a class="dash-btn dash-btn--escuro" href="medicoes.html"><i class="bi bi-heart-pulse"></i> Efetuar medição</a>
                </div>
            </article>`;
    }

    function heroComMedicao(m) {
        const normal = avaliacaoNormal(m.avaliacao);
        const variante = normal ? "dash-hero--ok" : "dash-hero--alerta";
        const icone = normal ? "bi-check2-circle" : "bi-exclamation-triangle-fill";
        const titulo = normal ? "Medição diária concluída." : "Medição de hoje fora do esperado.";
        const sub = normal
            ? "Já registou os seus valores de hoje."
            : `Avaliação: ${formatarAvaliacao(m.avaliacao)}`;
        const nota = normal
            ? `<div class="dash-hero__nota"><i class="bi bi-shield-check"></i> Os seus valores estão dentro dos limites esperados.</div>`
            : `<div class="dash-hero__nota"><i class="bi bi-info-circle"></i> Consulte o resultado para ver os detalhes.</div>`;
        return `
            <article class="dash-hero ${variante} dash-anim" style="animation-delay:.05s">
                <span class="dash-hoje"><i class="bi bi-check-circle-fill"></i> Concluída às ${m.hora}</span>
                <div class="dash-hero__top">
                    <div class="dash-hero__icone"><i class="bi ${icone}"></i></div>
                    <div>
                        <div class="dash-hero__titulo">${titulo}</div>
                        <div class="dash-hero__sub">${sub}</div>
                    </div>
                </div>
                ${tilesMedicao(m, "hero")}
                ${nota}
                <div class="dash-acoes">
                    <a class="dash-btn dash-btn--branco" href="dadosmedicos.html"><i class="bi bi-clipboard2-data"></i> Ver resultado</a>
                    <a class="dash-btn dash-btn--fantasma-claro" href="medicoes.html"><i class="bi bi-arrow-repeat"></i> Nova medição</a>
                </div>
            </article>`;
    }

    function cardProximaConsulta(c) {
        if (!c) {
            return `
                <section class="dash-card dash-anim" style="animation-delay:.12s">
                    <div class="dash-card__head">
                        <h2><span class="dash-chip-icone"><i class="bi bi-calendar-check"></i></span> Próxima consulta</h2>
                        <a class="dash-link" href="marcarconsulta.html">Marcar consulta <i class="bi bi-chevron-right"></i></a>
                    </div>
                    <div class="dash-vazio"><i class="bi bi-calendar-x"></i> Não tem consultas agendadas.</div>
                </section>`;
        }
        const medico = c.medico
            ? `<div class="dash-consulta__medico"><b>${c.medico}</b><span>${c.especialidade || ""}</span></div>`
            : `<div class="dash-consulta__medico"><b>Médico a atribuir</b></div>`;
        const estado = NOMES_ESTADO[c.estado] || c.estado || "";
        return `
            <section class="dash-card dash-anim" style="animation-delay:.12s">
                <div class="dash-card__head">
                    <h2><span class="dash-chip-icone"><i class="bi bi-calendar-check"></i></span> Próxima consulta</h2>
                    <a class="dash-link" href="marcacoes.html">Ver consultas <i class="bi bi-chevron-right"></i></a>
                </div>
                <div class="dash-consulta__quando">
                    <span class="dash-consulta__data">${dataLonga(c.data)}</span>
                    ${c.hora ? `<span class="dash-consulta__hora"><i class="bi bi-clock"></i> ${c.hora}</span>` : ""}
                </div>
                <div class="dash-sep"></div>
                <div class="dash-card__head">
                    ${medico}
                    ${estado ? `<span class="dash-badge dash-badge--ok"><i class="bi bi-check2"></i> ${estado}</span>` : ""}
                </div>
            </section>`;
    }

    function cardUltimaMedicao(m) {
        const head = `
            <div class="dash-card__head">
                <h2><span class="dash-chip-icone"><i class="bi bi-activity"></i></span> Última medição</h2>
                <a class="dash-link" href="dadosmedicos.html">Ver histórico <i class="bi bi-chevron-right"></i></a>
            </div>`;
        if (!m) {
            return `
                <section class="dash-card dash-anim" style="animation-delay:.19s">
                    ${head}
                    <div class="dash-vazio"><i class="bi bi-clipboard2"></i> Ainda não tem medições registadas.</div>
                </section>`;
        }
        const normal = avaliacaoNormal(m.avaliacao);
        return `
            <section class="dash-card dash-anim" style="animation-delay:.19s">
                ${head}
                ${tilesMedicao(m, "card")}
                <div class="dash-rodape">
                    <span class="dash-rodape__data"><i class="bi bi-clock-history"></i> ${dataLonga(m.data)} · ${m.hora}</span>
                    <span class="dash-aval ${normal ? "dash-aval--ok" : "dash-aval--alerta"}"><i class="bi bi-circle-fill" style="font-size:.5rem"></i> ${formatarAvaliacao(m.avaliacao)}</span>
                </div>
            </section>`;
    }

    function botaoHistorico() {
        return `<a class="dash-btn-largo dash-anim" style="animation-delay:.19s" href="dadosmedicos.html"><i class="bi bi-clock-history"></i> Ver histórico de medições</a>`;
    }

    // ---- Render ----
    function render(data) {
        // cabecalho fixo (foto e nome) a partir do mesmo pedido
        const nomeHeader = document.getElementById("nome-paciente");
        if (nomeHeader) nomeHeader.textContent = data.utilizador.nome || "";
        carregarFotoPerfil(document.getElementById("header-foto"), data.utilizador.fotoPerfil);

        const feitaHoje = data.medicaoDeHoje && data.ultimaMedicao;
        const hero = feitaHoje ? heroComMedicao(data.ultimaMedicao) : heroSemMedicao();

        // se ja mediu hoje, a "ultima medicao" e a propria do heroi: nao se repete
        // o cartao; fica apenas o acesso ao historico.
        const rodape = feitaHoje ? botaoHistorico() : cardUltimaMedicao(data.ultimaMedicao);

        const cont = document.getElementById("dash");
        cont.innerHTML = cabecalho(data.utilizador) + hero + cardProximaConsulta(data.proximaConsulta) + rodape;

        // foto do cabecalho de conteudo
        carregarFotoPerfil(document.getElementById("dash-foto"), data.utilizador.fotoPerfil);
    }

    function mostrarErro() {
        const cont = document.getElementById("dash");
        cont.innerHTML = `
            <div class="dash-erro dash-anim">
                <i class="bi bi-wifi-off"></i>
                <p>Não foi possível carregar o dashboard.</p>
                <button id="dash-retry">Tentar novamente</button>
            </div>`;
        document.getElementById("dash-retry").addEventListener("click", carregar);
    }

    async function carregar() {
        const cont = document.getElementById("dash");
        cont.innerHTML = `<div class="dash-loader"></div>`;
        try {
            const resp = await fetch(API_BASE_URL + "/dashboard", { credentials: "include" });
            if (resp.status === 401) { window.location.href = "../index.html"; return; }
            if (!resp.ok) throw new Error();
            const data = await resp.json();
            render(data);
        } catch (erro) {
            mostrarErro();
            if (window.toast) toast.error("Não foi possível carregar o dashboard.");
        }
    }

    document.addEventListener("DOMContentLoaded", carregar);
})();
