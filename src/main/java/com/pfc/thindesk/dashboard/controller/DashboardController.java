package com.pfc.thindesk.dashboard.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pfc.thindesk.dashboard.dto.DashboardResposta;
import com.pfc.thindesk.dashboard.exception.PeriodoInvalidoException;
import com.pfc.thindesk.dashboard.model.PeriodoFiltro;
import com.pfc.thindesk.dashboard.service.DashboardService;
import com.pfc.thindesk.usuarios.Usuario;

import lombok.RequiredArgsConstructor;

/**
 * Tela inicial do sistema: a dashboard com os indicadores e a evolução dos
 * chamados no período escolhido.
 *
 * @author Enrico Bertozzi
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Exibe a dashboard. Período inválido volta para 30 dias com aviso na tela.
     *
     * @param dias    período em dias
     * @param usuario usuário logado
     * @param model   modelo da tela
     *
     * @return view da dashboard
     */
    @GetMapping("/")
    public String inicio(@RequestParam(defaultValue = "30") int dias,
            @AuthenticationPrincipal Usuario usuario, Model model) {
        PeriodoFiltro periodo;
        try {
            periodo = PeriodoFiltro.de(dias);
        } catch (PeriodoInvalidoException e) {
            periodo = PeriodoFiltro.DIAS_30;
            model.addAttribute("erro", e.getMessage());
        }
        DashboardResposta dashboard = dashboardService.montar(usuario, periodo);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("serie", dashboard.serie());
        model.addAttribute("periodos", PeriodoFiltro.values());
        return "dashboard";
    }
}
