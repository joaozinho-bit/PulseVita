const pacientesMedico = [
    {
        id: 1,
        nome: "Ana Paula",
        idade: 32,
        genero: "Feminino",
        telefone: "+351 999 888 777",
        email: "anapaula@gmail.com",
        consultas: 15,
        temperatura: "37,6 ºC",
        batimentos: "93 bpm",
        ultimaMedicao: "Hoje às 09:42"
    },
    {
        id: 2,
        nome: "André Silva",
        idade: 28,
        genero: "Masculino",
        telefone: "+351 912 345 678",
        email: "andresilva@gmail.com",
        consultas: 9,
        temperatura: "36,8 ºC",
        batimentos: "81 bpm",
        ultimaMedicao: "Ontem às 16:20"
    },
    {
        id: 3,
        nome: "Marta Lopes",
        idade: 24,
        genero: "Feminino",
        telefone: "+351 934 567 123",
        email: "martalopes@gmail.com",
        consultas: 12,
        temperatura: "38,1 ºC",
        batimentos: "97 bpm",
        ultimaMedicao: "Hoje às 11:05"
    },
    {
        id: 4,
        nome: "Diogo Santos",
        idade: 35,
        genero: "Masculino",
        telefone: "+351 961 456 222",
        email: "diogosantos@gmail.com",
        consultas: 7,
        temperatura: "36,5 ºC",
        batimentos: "76 bpm",
        ultimaMedicao: "12/05/2026 às 18:30"
    },
    {
        id: 5,
        nome: "Carolina Martins",
        idade: 22,
        genero: "Feminino",
        telefone: "+351 968 333 222",
        email: "carolinamartins@gmail.com",
        consultas: 15,
        temperatura: "37,2 ºC",
        batimentos: "89 bpm",
        ultimaMedicao: "Hoje às 08:10"
    },
    {
        id: 6,
        nome: "Rui Costa",
        idade: 42,
        genero: "Masculino",
        telefone: "+351 912 789 333",
        email: "ruicosta@gmail.com",
        consultas: 6,
        temperatura: "36,9 ºC",
        batimentos: "84 bpm",
        ultimaMedicao: "10/05/2026 às 14:15"
    },
    {
        id: 7,
        nome: "Joana Ferreira",
        idade: 35,
        genero: "Feminino",
        telefone: "+351 933 987 111",
        email: "joanaferreira@gmail.com",
        consultas: 11,
        temperatura: "37,0 ºC",
        batimentos: "78 bpm",
        ultimaMedicao: "Hoje às 10:50"
    },
    {
        id: 8,
        nome: "Tiago Almeida",
        idade: 30,
        genero: "Masculino",
        telefone: "+351 915 222 444",
        email: "tiagoalmeida@gmail.com",
        consultas: 15,
        temperatura: "36,7 ºC",
        batimentos: "80 bpm",
        ultimaMedicao: "Ontem às 12:40"
    }
];

let pacienteSelecionadoMedico = pacientesMedico[0];

window.iniciarPacientesMedico = function () {
    const lista = document.getElementById("listaPacientesMedico");
    const pesquisa = document.getElementById("pesquisaPacienteMedico");

    if (!lista || !pesquisa) return;

    renderizarListaPacientesMedico(pacientesMedico);
    renderizarDadosPacienteMedico(pacienteSelecionadoMedico);

    pesquisa.addEventListener("input", () => {
        const texto = pesquisa.value.toLowerCase().trim();

        const filtrados = pacientesMedico.filter(p =>
            p.nome.toLowerCase().includes(texto) ||
            p.email.toLowerCase().includes(texto) ||
            p.telefone.toLowerCase().includes(texto)
        );

        renderizarListaPacientesMedico(filtrados);
    });
}

function renderizarListaPacientesMedico(listaPacientes) {
    const lista = document.getElementById("listaPacientesMedico");
    lista.innerHTML = "";

    if (listaPacientes.length === 0) {
        lista.innerHTML = `<p class="sem-paciente-selecionado">Nenhum paciente encontrado.</p>`;
        return;
    }

    listaPacientes.forEach(paciente => {
        const item = document.createElement("button");
        item.type = "button";
        item.classList.add("item-paciente-medico");

        if (pacienteSelecionadoMedico && pacienteSelecionadoMedico.id === paciente.id) {
            item.classList.add("ativo");
        }

        item.innerHTML = `
            <div class="icone-paciente-lista"></div>
            <span class="nome-paciente-lista">${paciente.nome}</span>
            <span class="consultas-paciente-lista">${paciente.consultas} Consultas</span>
            <span class="seta-paciente-lista">→</span>
        `;

        item.addEventListener("click", () => {
            pacienteSelecionadoMedico = paciente;
            renderizarListaPacientesMedico(listaPacientes);
            renderizarDadosPacienteMedico(paciente);
        });

        lista.appendChild(item);
    });
}

function renderizarDadosPacienteMedico(paciente) {
    const dados = document.getElementById("dadosPacienteMedico");

    if (!paciente) {
        dados.innerHTML = `<p class="sem-paciente-selecionado">Seleciona um paciente.</p>`;
        return;
    }

    dados.innerHTML = `
        <p class="secao-titulo">Dados Pessoais</p>
        <p class="paciente-nome">${paciente.nome}</p>
        <p class="paciente-info">${paciente.idade} Anos</p>
        <p class="paciente-info">${paciente.genero}</p>
        <p class="paciente-info">${paciente.telefone}</p>
        <p class="paciente-info">${paciente.email}</p>

        <div class="linha-divisoria"></div>

        <p class="secao-titulo">Dados Médicos</p>
        <p class="paciente-info">${paciente.consultas} Consultas</p>
        <p class="medicoes-label">Últimas medições</p>
        <p class="paciente-info">Temperatura: ${paciente.temperatura}</p>
        <p class="paciente-info">Batimentos: ${paciente.batimentos}</p>

        <button class="btn-enviar-mensagem" type="button">Enviar mensagem</button>
    `;
}