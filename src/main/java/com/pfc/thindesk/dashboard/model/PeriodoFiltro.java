package com.pfc.thindesk.dashboard.model;

import com.pfc.thindesk.dashboard.exception.PeriodoInvalidoException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Períodos de tempo aceitos pela dashboard. Cada opção representa a quantidade
 * de dias corridos, contando o dia de hoje.
 */
@Getter
@RequiredArgsConstructor
public enum PeriodoFiltro {

    DIAS_7(7, "Últimos 7 dias"),
    DIAS_30(30, "Últimos 30 dias"),
    DIAS_90(90, "Últimos 90 dias");

    private final int dias;
    private final String rotulo;

    /**
     * Localiza o período correspondente à quantidade de dias informada.
     *
     * @param dias quantidade de dias (7, 30 ou 90)
     * @return período correspondente
     * @throws PeriodoInvalidoException se a quantidade não for uma opção válida
     */
    public static PeriodoFiltro de(int dias) {
        for (PeriodoFiltro periodo : values()) {
            if (periodo.dias == dias) {
                return periodo;
            }
        }
        throw new PeriodoInvalidoException("Período inválido: use 7, 30 ou 90 dias.");
    }
}
