// Alternar sidebar em telas pequenas
document.querySelector('.toggle-btn').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('active');
});

// Alternar submenu
function toggleSubmenu(event, element) {
    event.preventDefault();
    element.parentElement.classList.toggle('open');
}

// Confirmação de exclusão em modal: formulários com data-confirmar só são enviados após confirmar
document.querySelectorAll('form[data-confirmar]').forEach((form) => {
    form.addEventListener('submit', (evento) => {
        evento.preventDefault();
        const modalEl = document.getElementById('modalConfirmar');
        if (!modalEl || typeof bootstrap === 'undefined') {
            return;
        }
        modalEl.querySelector('[data-mensagem]').textContent = form.dataset.confirmar;
        modalEl.querySelector('[data-acao-confirmar]').onclick = () => {
            bootstrap.Modal.getOrCreateInstance(modalEl).hide();
            form.submit();
        };
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    });
});