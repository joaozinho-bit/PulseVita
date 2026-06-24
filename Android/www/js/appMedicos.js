function abrirLinks(pagina) {
  fetch(`../pages-medico/${pagina}.html`)
    .then(r => r.text())
    .then(html => {
      const conteudo = document.getElementById('conteudo');

      conteudo.innerHTML = html;

      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = html;

      tempDiv.querySelectorAll('link[rel="stylesheet"]').forEach(link => {
        const href = link.getAttribute('href');

        if (!document.querySelector(`link[href="${href}"]`)) {
          const newLink = document.createElement('link');
          newLink.rel = 'stylesheet';
          newLink.href = href;
          document.head.appendChild(newLink);
        }
      });

      if (pagina === "calendario") {
        if (typeof window.iniciarCalendario === "function") {
          window.iniciarCalendario();
        }
      }

      if (pagina === "marcacoesMedico") {
        if (typeof window.iniciarMarcacoesMedico === "function") {
          window.iniciarMarcacoesMedico();
        }
      }

      if (pagina === "pacientes") {
        if (typeof window.iniciarPacientesMedico === "function") {
            window.iniciarPacientesMedico();
        }
      }
    })
    .catch(erro => {
      console.error("Erro ao carregar a página:", erro);
    });
}

window.onload = function () {
  abrirLinks("calendario");
};