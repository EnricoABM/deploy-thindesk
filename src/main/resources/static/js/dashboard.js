/**
 * Gráfico de evolução dos chamados na dashboard. Mostra um status por vez,
 * escolhido no seletor. Os dados vêm de window.serieDashboard, preenchido pelo
 * template; trocar o status não faz nova requisição.
 */
(function () {
    const serie = window.serieDashboard;
    const canvas = document.getElementById('graficoStatus');
    const seletor = document.getElementById('seletorStatus');
    if (!serie || !canvas || !seletor || typeof Chart === 'undefined') {
        return;
    }

    /** Cor de cada status, pela chave do enum. */
    const cores = {
        ABERTO: '#0d6efd',
        EM_ATENDIMENTO: '#fd7e14',
        FECHADO: '#198754',
        CANCELADO: '#dc3545'
    };

    /** Status mostrado ao abrir a tela. */
    const STATUS_INICIAL = 'EM_ATENDIMENTO';

    serie.linhas.forEach((linha) => {
        const opcao = document.createElement('option');
        opcao.value = linha.chave;
        opcao.textContent = linha.rotulo;
        opcao.selected = linha.chave === STATUS_INICIAL;
        seletor.appendChild(opcao);
    });

    const grafico = new Chart(canvas, {
        type: 'line',
        data: {
            labels: serie.datas,
            datasets: [{ label: '', data: [], borderWidth: 2, tension: 0.2 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: { legend: { display: false } },
            scales: {
                x: { title: { display: true, text: 'Data' } },
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 },
                    title: { display: true, text: 'Chamados' }
                }
            }
        }
    });

    /**
     * Mostra no gráfico a linha do status escolhido.
     *
     * @param chave nome do status, como ABERTO ou FECHADO
     */
    function mostrar(chave) {
        const linha = serie.linhas.find((l) => l.chave === chave);
        if (!linha) {
            return;
        }
        const cor = cores[chave] || '#6c757d';
        const dataset = grafico.data.datasets[0];
        dataset.label = linha.rotulo;
        dataset.data = linha.valores;
        dataset.borderColor = cor;
        dataset.backgroundColor = cor;
        grafico.update();
    }

    seletor.addEventListener('change', (evento) => mostrar(evento.target.value));
    mostrar(seletor.value);
})();
