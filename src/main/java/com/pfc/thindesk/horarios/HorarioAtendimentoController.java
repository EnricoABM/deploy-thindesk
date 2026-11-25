package com.pfc.thindesk.horarios;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pfc.thindesk.horarios.dto.HorarioForm;
import com.pfc.thindesk.horarios.dto.HorarioResponse;

import lombok.RequiredArgsConstructor;

/** Ajuste de horários de atendimento. Restrito ao administrador. */
@Controller
@RequestMapping("/ajustes-horarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class HorarioAtendimentoController {

    private final HorarioAtendimentoService horarioAtendimentoService;

    @GetMapping
    public String pagina(Model model) {
        model.addAttribute("horarios", horarioAtendimentoService.listarTodos().stream()
                .map(HorarioResponse::de)
                .toList());
        model.addAttribute("novoHorario", new HorarioForm());
        return "ajustes-horarios";
    }

    @PostMapping("/salvar")
    public String salvarHorario(@ModelAttribute HorarioForm form) {
        horarioAtendimentoService.salvar(form.paraEntidade());
        return "redirect:/ajustes-horarios";
    }

    @GetMapping("/deletar/{id}")
    public String deletarHorario(@PathVariable String id) {
        horarioAtendimentoService.deletar(id);
        return "redirect:/ajustes-horarios";
    }
}
