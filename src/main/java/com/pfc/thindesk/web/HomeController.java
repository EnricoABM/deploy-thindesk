package com.pfc.thindesk.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.pfc.thindesk.chamados.Chamado;
import com.pfc.thindesk.chamados.ChamadoService;
import com.pfc.thindesk.clientes.Cliente;
import com.pfc.thindesk.clientes.ClienteService;
import com.pfc.thindesk.horarios.HorarioAtendimento;
import com.pfc.thindesk.horarios.HorarioAtendimentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final HorarioAtendimentoService horarioAtendimentoService;

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("layout");
        String fragment = "home :: content";
        log.info("Carregando fragmento: {}", fragment); 
        modelAndView.addObject("content", fragment);
        return modelAndView;
    }

    @GetMapping("/chamados")
    public String chamados(Model model) {
        List<Chamado> chamados = chamadoService.listarChamados();
        model.addAttribute("chamados", chamados); 
        String fragment = "chamados :: content";
        log.info("Carregando fragmento: {}", fragment); 
        model.addAttribute("content", fragment);
        return "chamados";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        String fragment = "clientes :: content";
        log.info("Carregando fragmento: {}", fragment); 
        model.addAttribute("content", fragment);
        return "clientes";
    }

    @GetMapping("/ajustes-horarios")
    public String horarios(Model model) {
        List<HorarioAtendimento> horarios = horarioAtendimentoService.listarTodos();
        model.addAttribute("ajustes-horarios", horarios);
        String fragment = "ajustes-horarios :: content";
        log.info("Carregando fragmento: {}", fragment); 
        model.addAttribute("content", fragment);
        return "ajustes-horarios";
    }

}
