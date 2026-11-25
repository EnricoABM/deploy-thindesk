package com.pfc.thindesk.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

import com.pfc.thindesk.dashboard.model.PeriodoFiltro;

/**
 * Dados completos da dashboard para um usuário e um período.
 *
 * @param periodo  período escolhido no filtro
 * @param inicio   primeiro dia do período (inclusive)
 * @param fim      último dia do período (hoje, no fuso configurado)
 * @param cartoes  indicadores de situação, conforme o perfil
 * @param serie    evolução diária dos chamados por status
 */
public record DashboardResposta(
        PeriodoFiltro periodo,
        LocalDate inicio,
        LocalDate fim,
        List<Cartao> cartoes,
        SerieDiaria serie) {
}
