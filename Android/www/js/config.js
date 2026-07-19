// Endereco base do backend.
// Na app Android (Capacitor) "localhost" seria o proprio telemovel, por isso
// usa-se o IP do PC na rede local. No browser, usa-se o mesmo hostname com que
// a pagina foi aberta (localhost ou 127.0.0.1): com SameSite=Lax os cookies de
// sessao so sao aceites se a pagina e a API estiverem no mesmo site.
const API_BASE_URL = window.Capacitor
    ? "http://192.168.1.10:8080"
    : "http://" + location.hostname + ":8080";
