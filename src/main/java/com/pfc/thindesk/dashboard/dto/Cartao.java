package com.pfc.thindesk.dashboard.dto;

/**
 * Indicador exibido em um card da dashboard.
 *
 * @param rotulo texto do card
 * @param valor  quantidade de chamados no card
 */
public record Cartao(String rotulo, long valor) {
}
