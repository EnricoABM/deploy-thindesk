package com.pfc.thindesk.chamados;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Situações de um chamado. ABERTO e EM_ATENDIMENTO são estados ativos; FECHADO e
 * CANCELADO são finais, sem transição de saída.
 *
 * @author Enrico Bertozzi
 */
@Getter
@RequiredArgsConstructor
public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em atendimento"),
    FECHADO("Fechado"),
    CANCELADO("Cancelado");

    private final String rotulo;
}
