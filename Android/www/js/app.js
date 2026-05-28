async function carregarPagina(pagina) {

    const resposta = await fetch(`../pages/${pagina}.html`);

    const html = await resposta.text();

    document.getElementById("conteudo").innerHTML = html;
}