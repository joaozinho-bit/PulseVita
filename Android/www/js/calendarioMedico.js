const CONSULTAS = {
  "2026-06-08 09:00": { nome: "Ana Costa"      },
  "2026-06-08 10:00": { nome: "Marta Reis"     },
  "2026-06-08 11:00": { nome: "João Pinto"     },
  "2026-06-08 12:00": { nome: "Rita Alves"     },
  "2026-06-08 15:00": { nome: "Rui Barros"     },
  "2026-06-08 16:00": { nome: "Clara Teixeira" },

  "2026-06-09 13:00": { nome: "Luís Ferreira"  },
  "2026-06-09 14:00": { nome: "Inês Sousa"     },
  "2026-06-09 17:00": { nome: "André Rocha"    },
  "2026-06-09 18:00": { nome: "Diogo Santos"   },

  "2026-06-10 15:00": { nome: "Vera Simões"    },
  "2026-06-10 16:00": { nome: "Hugo Vieira"    },
  "2026-06-10 17:00": { nome: "Filipa Moura"   },
  "2026-06-10 18:00": { nome: "Lena Pires"     },

  "2026-06-11 09:00": { nome: "Bruno Matos"    },
  "2026-06-11 10:00": { nome: "Carla Neves"    },
  "2026-06-11 11:00": { nome: "Sofia Lopes"    },
  "2026-06-11 12:00": { nome: "Pedro Gomes"    },

  "2026-06-12 15:00": { nome: "Nuno Faria"     },
  "2026-06-12 16:00": { nome: "Beatriz Cruz"   },
  "2026-06-12 17:00": { nome: "Sara Cunha"     },
  "2026-06-12 18:00": { nome: "Tomás Melo"     },
};

/* ------------------------------------------------------------------
   CONFIGURAÇÃO
   ------------------------------------------------------------------ */
const HORAS = [
  "08:00","09:00","10:00","11:00","12:00",
  "13:00","14:00","15:00","16:00","17:00","18:00","19:00"
];

const DIAS_ABBR = ["Seg","Ter","Qua","Qui","Sex"];
const MESES     = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                   "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"];

/* ------------------------------------------------------------------
   ESTADO
   ------------------------------------------------------------------ */
let offsetSemana = 0;

/* ------------------------------------------------------------------
   UTILITÁRIOS
   ------------------------------------------------------------------ */
function inicioSemana(offset) {
  const hoje = new Date();
  const diaSemana = hoje.getDay();
  const diff = diaSemana === 0 ? -6 : 1 - diaSemana;
  const seg = new Date(hoje);
  seg.setDate(hoje.getDate() + diff + offset * 7);
  seg.setHours(0, 0, 0, 0);
  return seg;
}

function formatarData(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function hojeStr() {
  return formatarData(new Date());
}

/* ------------------------------------------------------------------
   RENDER
   ------------------------------------------------------------------ */
function renderCalendario() {
  const seg = inicioSemana(offsetSemana);

  const datas = Array.from({ length: 5 }, (_, i) => {
    const d = new Date(seg);
    d.setDate(seg.getDate() + i);
    return d;
  });

  /* Label da semana */
  const inicio = `${datas[0].getDate()} ${MESES[datas[0].getMonth()]}`;
  const fim    = `${datas[4].getDate()} ${MESES[datas[4].getMonth()]} ${datas[4].getFullYear()}`;
  document.getElementById("semanaLabel").textContent = `${inicio} – ${fim}`;

  /* Cabeçalho de dias */
  const header = document.getElementById("calHeader");
  header.innerHTML = "";

  const timePad = document.createElement("div");
  timePad.className = "cal-header-time";
  header.appendChild(timePad);

  const hoje = hojeStr();
  datas.forEach((data, i) => {
    const cell = document.createElement("div");
    cell.className = "cal-header-day";
    const numStr  = String(data.getDate()).padStart(2, "0");
    const isToday = formatarData(data) === hoje;
    cell.innerHTML = `
      <div class="cal-day-abbr">${DIAS_ABBR[i]}</div>
      <div class="cal-day-num${isToday ? " is-today" : ""}">${numStr}</div>
    `;
    header.appendChild(cell);
  });

  /* Corpo: linhas por hora */
  const body = document.getElementById("calBody");
  body.innerHTML = "";

  HORAS.forEach(hora => {
    const row = document.createElement("div");
    row.className = "cal-row";

    const tc = document.createElement("div");
    tc.className = "cal-time";
    tc.textContent = hora;
    row.appendChild(tc);

    datas.forEach(data => {
      const chave   = `${formatarData(data)} ${hora}`;
      const consulta = CONSULTAS[chave] || null;

      const slot = document.createElement("div");
      slot.className = "cal-slot";

      if (consulta) {
        const card = document.createElement("div");
        card.className = "appt-card";
        card.innerHTML = `<div class="appt-nome">${consulta.nome}</div>`;
        slot.appendChild(card);
      }

      row.appendChild(slot);
    });

    body.appendChild(row);
  });

  /* Estatísticas */
  calcularStats(datas);
}

/* ------------------------------------------------------------------
   ESTATÍSTICAS
   ------------------------------------------------------------------ */
function calcularStats(datas) {
  const strDatas  = datas.map(d => formatarData(d));
  const hoje      = hojeStr();
  const agora     = new Date();
  const horaAgora = agora.getHours() * 60 + agora.getMinutes();

  let total        = 0;
  let countHoje    = 0;
  let proximaMin   = null;

  Object.keys(CONSULTAS).forEach(chave => {
    const [dataChave, horaChave] = chave.split(" ");
    if (!strDatas.includes(dataChave)) return;

    total++;

    if (dataChave === hoje) {
      countHoje++;
      const [h, m] = horaChave.split(":").map(Number);
      const minutos = h * 60 + m;
      if (minutos >= horaAgora && (proximaMin === null || minutos < proximaMin)) {
        proximaMin = minutos;
      }
    }
  });

  document.getElementById("statTotal").textContent = total;
  document.getElementById("statHoje").textContent  = countHoje;

  if (proximaMin !== null) {
    const hh = String(Math.floor(proximaMin / 60)).padStart(2, "0");
    const mm = String(proximaMin % 60).padStart(2, "0");
    document.getElementById("statProxima").textContent = `${hh}:${mm}`;
  } else {
    document.getElementById("statProxima").textContent = "—";
  }
}

function iniciarCalendario() {

    offsetSemana = 0;

    document.getElementById("btnAnterior").addEventListener("click", () => {
        offsetSemana--;
        renderCalendario();
    });

    document.getElementById("btnProxima").addEventListener("click", () => {
        offsetSemana++;
        renderCalendario();
    });

    document.getElementById("btnHoje").addEventListener("click", () => {
        offsetSemana = 0;
        renderCalendario();
    });

    renderCalendario();
}