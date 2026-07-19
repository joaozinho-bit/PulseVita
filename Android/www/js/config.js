// Endereco base do backend.
// Na app Android (Capacitor) "localhost" seria o proprio telemovel, por isso
// usa-se o IP do PC na rede local. No browser, usa-se o mesmo hostname com que
// a pagina foi aberta (localhost ou 127.0.0.1): com SameSite=Lax os cookies de
// sessao so sao aceites se a pagina e a API estiverem no mesmo site.
const API_BASE_URL = window.Capacitor
    ? "http://192.168.1.10:8080"
    : "http://" + location.hostname + ":8080";

// Carrega a foto de perfil do backend para um <img>.
// Na app o <img> nao pode apontar diretamente para http://... (mixed content:
// a WebView corre em https://localhost), por isso a imagem e descarregada por
// fetch, que o Capacitor encaminha pela camada nativa, e aplicada como blob.
// A classe "pronta" revela a imagem: ate la o CSS mostra um placeholder, para
// a imagem por defeito nunca aparecer por um instante antes da fotografia.
function carregarFotoPerfil(imgEl, nomeFicheiro) {
    if (!imgEl) return Promise.resolve(false);

    const revelar = () => imgEl.classList.add("pronta");

    if (!nomeFicheiro) {
        revelar();
        return Promise.resolve(false);
    }

    const url = API_BASE_URL + "/uploads/" + nomeFicheiro;

    return new Promise((resolve) => {
        const aplicar = (src) => {
            imgEl.onload = () => { revelar(); resolve(true); };
            imgEl.onerror = () => { revelar(); resolve(false); };
            imgEl.src = src;
        };

        if (!window.Capacitor) {
            aplicar(url);
            return;
        }
        fetch(url)
            .then(r => { if (!r.ok) throw new Error(r.status); return r.blob(); })
            .then(blob => aplicar(URL.createObjectURL(blob)))
            .catch(() => { revelar(); resolve(false); });
    });
}

// Poe um botao em estado de carregamento (desativado, com spinner) e devolve
// a funcao que repoe o estado original
function ativarLoaderBotao(botao) {
    const conteudoOriginal = botao.innerHTML;
    botao.disabled = true;
    botao.innerHTML = '<span class="loader-botao"></span>';
    return function reporBotao() {
        botao.disabled = false;
        botao.innerHTML = conteudoOriginal;
    };
}
