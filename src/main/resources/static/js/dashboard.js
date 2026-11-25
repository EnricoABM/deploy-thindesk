/**
 * Gráfico de evolução dos chamados por status, na dashboard.
 * Os dados vêm de window.serieDashboard, preenchido pelo template.
 */
(function () {
    const serie = window.serieDashboard;
    const canvas = document.getElementById('graficoStatus');
    if (!serie || !canvas || typeof Chart === 'undefined') {
        return;
    }

    const cores = ['#0d6efd', '#fd7e14', '#6f42c1', '#198754', '#dc3545'];

    new Chart(canvas, {
        type: 'line',
        data: {
            labels: serie.datas,
            datasets: serie.linhas.map((linha, i) => ({
                label: linha.rotulo,
                data: linha.valores,
                borderColor: cores[i % cores.length],
                backgroundColor: cores[i % cores.length],
                tension: 0.2
            }))
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
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
})();
