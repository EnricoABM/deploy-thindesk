package com.pfc.thindesk.dashboard.dto;

import java.util.List;

/**
 * Uma linha do gráfico: a quantidade de chamados em um status, dia a dia.
 *
 * @param chave   nome do status (ABERTO, EM_ATENDIMENTO, FECHADO, CANCELADO), usado pelo seletor
 * @param rotulo  nome do status exibido na tela
 * @param valores quantidade por dia, na mesma ordem das datas da série
 */
public record LinhaStatus(String chave, String rotulo, List<Long> valores) {
}
