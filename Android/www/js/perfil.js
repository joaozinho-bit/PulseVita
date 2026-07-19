// Header comum a todas as paginas da app: carrega o nome e a fotografia do
// utilizador autenticado, para nenhuma pagina repetir esta logica.
// Em caso de erro apenas revela a imagem por defeito; o redirecionamento
// para o login continua a cargo do guard de cada pagina.
document.addEventListener("DOMContentLoaded", async function () {
    const headerFoto = document.getElementById("header-foto");
    if (!headerFoto) return;

    try {
        const resp = await fetch(API_BASE_URL + "/users/me", { credentials: "include" });
        if (!resp.ok) {
            headerFoto.classList.add("pronta");
            return;
        }
        const dados = await resp.json();

        const nome = document.getElementById("nome-paciente");
        if (nome && dados.nomeCompleto) nome.textContent = dados.nomeCompleto;

        carregarFotoPerfil(headerFoto, dados.fotoPerfil);
    } catch (erro) {
        headerFoto.classList.add("pronta");
    }
});
