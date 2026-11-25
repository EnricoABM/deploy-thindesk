package com.pfc.thindesk.dashboard.dto;

import java.util.List;

/**
 * Uma linha do gráfico: a quantidade de chamados em um status, dia a dia.
 *
 * @param rotulo  nome do status exibido na legenda
 * @param valores quantidade por dia, na mesma ordem das datas da série
 */
public record LinhaStatus(String rotulo, List<Long> valores) {
}
