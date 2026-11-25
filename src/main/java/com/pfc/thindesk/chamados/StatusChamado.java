package com.pfc.thindesk.chamados;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em atendimento"),
    AGUARDANDO_CLIENTE("Aguardando cliente"),
    RESOLVIDO("Resolvido"),
    FECHADO("Fechado");

    private final String rotulo;
}
