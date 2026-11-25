package com.pfc.thindesk.dashboard.dto;

import java.util.List;

/**
 * Evolução diária dos chamados por status, usada no gráfico.
 *
 * @param datas  rótulos do eixo X (dia/mês), um por dia do período
 * @param linhas uma linha por status
 */
public record SerieDiaria(List<String> datas, List<LinhaStatus> linhas) {
}
