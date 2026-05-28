// Função para carregar o conteúdo da página de contacto
async function carregarPaginaDeResultados(pagina) {

    const resposta = await fetch(`../pages/pages-resultado/${pagina}.html`);

    const html = await resposta.text();

    document.getElementById("conteudo").innerHTML = html;
}

// Função para carregar páginas específicas dentro da seção "Contactar-nos"
async function carregarPaginaDeContacto(pagina) {

    const resposta = await fetch(`../pages/pages-contactar/${pagina}.html`);

    const html = await resposta.text();

    document.getElementById("conteudo").innerHTML = html;
}

