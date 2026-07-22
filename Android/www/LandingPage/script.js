const background = document.querySelector(".background-interativo");
const loadingScreen = document.getElementById("loadingScreen");
const header = document.querySelector(".header");

// Efeito do rato
background.addEventListener("mousemove", (e) => {
    const rect = background.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    background.style.setProperty("--mouse-x", `${x}px`);
    background.style.setProperty("--mouse-y", `${y}px`);
});

// Loading: espera a página carregar completamente
window.addEventListener("load", () => {
    // Pequeno delay para o efeito ser visível mesmo em carregamentos rápidos
    setTimeout(() => {
        // Esconde o loading
        loadingScreen.classList.add("hidden");

        // Dispara a animação do header
        header.classList.add("animado");
    }, 600);
});

// Abrir e fechar especificações
const itens = document.querySelectorAll('.item-especificacao');

itens.forEach(item => {
    const botao = item.querySelector('.btn-toggle');

    botao.addEventListener('click', () => {
        item.classList.toggle('ativo');

        if (item.classList.contains('ativo')) {
            botao.textContent = '−';
        } else {
            botao.textContent = '+';
        }
    });
});

//utilizadore de like e compra fake

const usuarios = [
    { nome: "Pedro Silva", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=1" },
    { nome: "Leticia Santos", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=2" },
    { nome: "Flavio Campus", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=3" },
    { nome: "Ana Ribeiro", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=4" },
    { nome: "Carlos Mendes", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=5" },
    { nome: "Mariana Costa", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=6" },
    { nome: "Rui Almeida", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=7" },
    { nome: "Beatriz Lopes", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=8" },
    { nome: "Tiago Ferreira", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=9" },
    { nome: "Sofia Martins", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=10" },
    { nome: "Diogo Pereira", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=11" },
    { nome: "Catarina Sousa", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=12" },
    { nome: "Miguel Rocha", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=13" },
    { nome: "Inês Carvalho", acao: "Comprou esse produto", icone: "../Imagens/Elementos/Carrinho.png", avatar: "https://i.pravatar.cc/300?img=14" },
    { nome: "Bruno Tavares", acao: "Deu like nesse produto", icone: "../Imagens/Elementos/Coracao.png", avatar: "https://i.pravatar.cc/300?img=15" },
];

function iniciarNotificacoes(seletor, listaUsuarios, intervalo) {
    const elemento = document.querySelector(seletor);
    if (!elemento) return;

    const avatarImg = elemento.querySelector('.img-utilizador');
    const iconeImg = elemento.querySelector('.icons');
    const nomeEl = elemento.querySelector('.conteudo .box h1');
    const acaoEl = elemento.querySelector('.conteudo .box p');

    let index = Math.floor(Math.random() * listaUsuarios.length);

    function atualizar() {
        elemento.classList.add('fade');

        setTimeout(() => {
            index = (index + 1) % listaUsuarios.length;
            const usuario = listaUsuarios[index];

            avatarImg.src = usuario.avatar;
            iconeImg.src = usuario.icone;
            nomeEl.textContent = usuario.nome;
            acaoEl.textContent = usuario.acao;

            elemento.classList.remove('fade');
        }, 400);
    }

    const inicial = listaUsuarios[index];
    avatarImg.src = inicial.avatar;
    iconeImg.src = inicial.icone;
    nomeEl.textContent = inicial.nome;
    acaoEl.textContent = inicial.acao;

    setInterval(atualizar, intervalo);
}

iniciarNotificacoes('.pessoa-topo', usuarios, 4000);
iniciarNotificacoes('.pessoa-footer', usuarios, 5500);


document.querySelectorAll('.faq-item').forEach(item => {
    item.addEventListener('click', () => {
        const jaEstaAtivo = item.classList.contains('ativo');

        // Fecha todos
        document.querySelectorAll('.faq-item').forEach(i => {
            i.classList.remove('ativo');
            i.querySelector('.faq-btn').textContent = '+';
        });

        // Abre o clicado (se não estava já aberto)
        if (!jaEstaAtivo) {
            item.classList.add('ativo');
            item.querySelector('.faq-btn').textContent = '−';
        }
    });
});

// ===== Botão voltar ao topo =====
const btnTopo = document.getElementById("btnTopo");

window.addEventListener("scroll", () => {
    if (window.scrollY > 400) {
        btnTopo.classList.add("visivel");
    } else {
        btnTopo.classList.remove("visivel");
    }
});

btnTopo.addEventListener("click", () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
});