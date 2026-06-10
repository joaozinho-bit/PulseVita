function abrirLinks(pagina) {
  fetch(`../pages-medico/${pagina}.html`)
    .then(r => r.text())
    .then(html => {
      const conteudo = document.getElementById('conteudo');

      // Injeta o HTML (sem executar scripts ainda)
      conteudo.innerHTML = html;

      if (pagina === "calendario") {
      iniciarCalendario();
}

      // 1. Carrega os <link> CSS que estejam no HTML injetado
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = html;

      tempDiv.querySelectorAll('link[rel="stylesheet"]').forEach(link => {
        // Só adiciona se ainda não estiver carregado
        const href = link.getAttribute('href');
        if (!document.querySelector(`link[href="${href}"]`)) {
          const newLink = document.createElement('link');
          newLink.rel = 'stylesheet';
          newLink.href = href;
          document.head.appendChild(newLink);
        }
      });
    });
}

window.onload = function () {
  abrirLinks("calendario");
};