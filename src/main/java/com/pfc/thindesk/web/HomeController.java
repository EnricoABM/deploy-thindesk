package com.pfc.thindesk.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.pfc.thindesk.chamados.ChamadoService;
import com.pfc.thindesk.chamados.dto.ChamadoResponse;
import com.pfc.thindesk.clientes.ClienteService;
import com.pfc.thindesk.clientes.dto.ClienteResponse;
import com.pfc.thindesk.usuarios.Usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("layout");
        String fragment = "home :: content";
        log.info("Carregando fragmento: {}", fragment);
        modelAndView.addObject("content", fragment);
        return modelAndView;
    }

    @GetMapping("/chamados")
    public String chamados(Model model, @AuthenticationPrincipal Usuario usuario) {
        List<ChamadoResponse> chamados = chamadoService.listarVisiveis(usuario).stream()
                .map(ChamadoResponse::de)
                .toList();
        model.addAttribute("chamados", chamados);
        String fragment = "chamados :: content";
        log.info("Carregando fragmento: {}", fragment);
        model.addAttribute("content", fragment);
        return "chamados";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<ClienteResponse> clientes = clienteService.listarClientes().stream()
                .map(ClienteResponse::de)
                .toList();
        model.addAttribute("clientes", clientes);
        String fragment = "clientes :: content";
        log.info("Carregando fragmento: {}", fragment);
        model.addAttribute("content", fragment);
        return "clientes";
    }
}
