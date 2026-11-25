package com.pfc.thindesk.clientes;

import java.util.NoSuchElementException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pfc.thindesk.clientes.dto.ClienteRequest;
import com.pfc.thindesk.clientes.dto.ClienteResponse;
import com.pfc.thindesk.clientes.exception.ClienteInvalidoException;

import lombok.RequiredArgsConstructor;

/**
 * Controller do formulário de cadastro de clientes.
 * 
 * @author Enrico Bertozzi
 */
@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteWebController {

    private final ClienteService clienteService;

    /**
     * Exibe o formulário de novo cliente.
     * 
     * @param model modelo da tela
     * 
     * @return view do formulário
     */
    @GetMapping("/novo")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteRequest());
        preencherFormulario(model, false, null);
        return "cliente-form";
    }

    /**
     * Cadastra o cliente do formulário.
     * 
     * @param form     dados enviados
     * @param model    modelo para erros
     * @param redirect mensagens de sucesso
     * 
     * @return redirecionamento ou formulário com erro
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String cadastrar(@ModelAttribute("form") ClienteRequest form, Model model, RedirectAttributes redirect) {
        try {
            clienteService.novoCliente(form.paraEntidade());
            redirect.addFlashAttribute("sucesso", "Cliente '" + form.getNome().trim() + "' cadastrado.");
            return "redirect:/clientes";
        } catch (ClienteInvalidoException e) {
            preencherFormulario(model, false, null);
            model.addAttribute("erro", e.getMessage());
            return "cliente-form";
        }
    }

    /**
     * Lista os clientes filtrados por nome e setor.
     * 
     * @param nome  trecho do nome, opcional
     * @param setor setor escolhido, opcional
     * @param model modelo da tela
     * 
     * @return view da lista
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) String setor, Model model) {
        model.addAttribute("clientes", clienteService.filtrar(nome, setor).stream().map(ClienteResponse::de).toList());
        model.addAttribute("nome", nome == null ? "" : nome);
        model.addAttribute("setorFiltro", setor == null ? "" : setor);
        model.addAttribute("setores", clienteService.listarSetores());
        return "clientes";
    }

    /**
     * Exibe o formulário de edição de um cliente.
     * 
     * @param id       identificador do cliente
     * @param model    modelo da tela
     * @param redirect mensagem se o cliente não existir
     * 
     * @return view do formulário ou redirecionamento
     */
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", ClienteRequest.de(clienteService.buscarCliente(id)));
            preencherFormulario(model, true, id);
            return "cliente-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/clientes";
        }
    }

    /**
     * Atualiza um cliente com os dados do formulário.
     * 
     * @param id       identificador do cliente
     * @param form     dados enviados
     * @param model    modelo para erros
     * @param redirect mensagens de sucesso ou falha
     * 
     * @return redirecionamento ou formulário com erro
     */
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String atualizar(@PathVariable String id, @ModelAttribute("form") ClienteRequest form,
            Model model, RedirectAttributes redirect) {
        try {
            clienteService.atualizarCliente(id, form.paraEntidade());
            redirect.addFlashAttribute("sucesso", "Cliente '" + form.getNome().trim() + "' atualizado.");
            return "redirect:/clientes";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/clientes";
        } catch (ClienteInvalidoException e) {
            preencherFormulario(model, true, id);
            model.addAttribute("erro", e.getMessage());
            return "cliente-form";
        }
    }

    /**
     * Remove um cliente pelo id.
     * 
     * @param id       identificador do cliente
     * @param redirect mensagem de sucesso ou falha
     * 
     * @return redirecionamento para a lista
     */
    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String excluir(@PathVariable String id, RedirectAttributes redirect) {
        try {
            clienteService.excluirCliente(id);
            redirect.addFlashAttribute("sucesso", "Cliente removido.");
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }

    /**
     * Preenche o modo e a ação do formulário compartilhado.
     * 
     * @param model  modelo da tela
     * @param edicao verdadeiro na edição
     * @param id     identificador do cliente, nulo no cadastro
     */
    private void preencherFormulario(Model model, boolean edicao, String id) {
        model.addAttribute("modoEdicao", edicao);
        model.addAttribute("acao", edicao ? "/clientes/" + id + "/editar" : "/clientes");
    }
}
