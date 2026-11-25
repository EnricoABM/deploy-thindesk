package com.pfc.thindesk.chamados;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pfc.thindesk.chamados.dto.AlteracaoStatusRequest;
import com.pfc.thindesk.chamados.dto.ChamadoRequest;
import com.pfc.thindesk.chamados.dto.ChamadoResponse;
import com.pfc.thindesk.chamados.exception.ChamadoInvalidoException;
import com.pfc.thindesk.clientes.ClienteService;
import com.pfc.thindesk.usuarios.Usuario;
import com.pfc.thindesk.usuarios.UsuarioService;

import lombok.RequiredArgsConstructor;

/**
 * Controller das telas de chamados: lista, abertura, detalhe, edição,
 * atribuição e status.
 * 
 * @author Enrico Bertozzi
 */
@Controller
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoWebController {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    /**
     * Lista os chamados visíveis ao usuário, filtrados por título e status.
     * 
     * @param titulo  trecho do título, opcional
     * @param status  status escolhido, opcional
     * @param usuario usuário logado
     * @param model   modelo da tela
     * 
     * @return view da lista
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String titulo,
            @RequestParam(required = false) StatusChamado status,
            @AuthenticationPrincipal Usuario usuario, Model model) {
        List<ChamadoResponse> chamados = chamadoService.filtrar(usuario, titulo, status).stream()
                .map(ChamadoResponse::de)
                .toList();
        model.addAttribute("chamados", chamados);
        model.addAttribute("nomes", nomes());
        model.addAttribute("titulo", titulo == null ? "" : titulo);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("statusOpcoes", StatusChamado.values());
        return "chamados";
    }

    /**
     * Exibe o formulário de novo chamado.
     * 
     * @param model modelo da tela
     * 
     * @return view do formulário
     */
    @GetMapping("/novo")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String novo(Model model) {
        ChamadoRequest form = new ChamadoRequest();
        form.setPrioridade(Prioridade.MEDIA);
        model.addAttribute("form", form);
        preencherFormulario(model, false, null);
        return "chamado-form";
    }

    /**
     * Abre o chamado do formulário.
     * 
     * @param form     dados enviados
     * @param usuario  usuário que abre
     * @param model    modelo para erros
     * @param redirect mensagem de sucesso
     * 
     * @return redirecionamento ao detalhe ou formulário com erro
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String cadastrar(@ModelAttribute("form") ChamadoRequest form,
            @AuthenticationPrincipal Usuario usuario, Model model, RedirectAttributes redirect) {
        try {
            Chamado criado = chamadoService.criarChamado(form.paraEntidade(), usuario);
            redirect.addFlashAttribute("sucesso", "Chamado '" + criado.getTitulo() + "' aberto.");
            return "redirect:/chamados/" + criado.getId();
        } catch (ChamadoInvalidoException | NoSuchElementException e) {
            preencherFormulario(model, false, null);
            model.addAttribute("erro", e.getMessage());
            return "chamado-form";
        }
    }

    /**
     * Exibe o detalhe de um chamado visível ao usuário, com histórico e ações
     * permitidas.
     * 
     * @param id       identificador do chamado
     * @param usuario  usuário logado
     * @param model    modelo da tela
     * @param redirect mensagem se o chamado não existir
     * 
     * @return view do detalhe ou redirecionamento
     */
    @GetMapping("/{id}")
    public String detalhar(@PathVariable String id, @AuthenticationPrincipal Usuario usuario,
            Model model, RedirectAttributes redirect) {
        try {
            Chamado chamado = chamadoService.buscarVisivel(id, usuario);
            model.addAttribute("chamado", ChamadoResponse.de(chamado));
            model.addAttribute("nomes", nomes());
            model.addAttribute("podeGerenciar", chamadoService.podeGerenciar(chamado, usuario));
            model.addAttribute("podeEditar", chamadoService.podeEditar(chamado, usuario));
            model.addAttribute("podeAssumir", chamadoService.podeAssumir(chamado, usuario));
            model.addAttribute("destinos", chamadoService.proximosStatus(chamado.getStatus()));
            return "chamado-detalhe";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chamados";
        }
    }

    /**
     * Exibe o formulário de edição de um chamado que o usuário pode
     * gerenciar.
     * 
     * @param id       identificador do chamado
     * @param usuario  usuário logado
     * @param model    modelo da tela
     * @param redirect mensagem se o chamado não existir
     * 
     * @return view do formulário ou redirecionamento
     */
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String editar(@PathVariable String id, @AuthenticationPrincipal Usuario usuario,
            Model model, RedirectAttributes redirect) {
        try {
            Chamado chamado = chamadoService.buscarEditavel(id, usuario);
            model.addAttribute("form", ChamadoRequest.de(chamado));
            preencherFormulario(model, true, id);
            return "chamado-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chamados";
        }
    }

    /**
     * Atualiza título, descrição, tipo e prioridade do chamado.
     * 
     * @param id       identificador do chamado
     * @param form     dados enviados
     * @param usuario  usuário logado
     * @param model    modelo para erros
     * @param redirect mensagens de sucesso ou falha
     * 
     * @return redirecionamento ao detalhe ou formulário com erro
     */
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public String atualizar(@PathVariable String id, @ModelAttribute("form") ChamadoRequest form,
            @AuthenticationPrincipal Usuario usuario, Model model, RedirectAttributes redirect) {
        try {
            chamadoService.atualizarChamado(id, form.paraEntidade(), usuario);
            redirect.addFlashAttribute("sucesso", "Chamado atualizado.");
            return "redirect:/chamados/" + id;
        } catch (ChamadoInvalidoException e) {
            preencherFormulario(model, true, id);
            model.addAttribute("erro", e.getMessage());
            return "chamado-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chamados";
        }
    }

    /**
     * Atribui o chamado ao técnico ou administrador logado.
     * 
     * @param id       identificador do chamado
     * @param usuario  usuário logado
     * @param redirect mensagem de sucesso ou falha
     * 
     * @return redirecionamento ao detalhe
     */
    @PostMapping("/{id}/assumir")
    @PreAuthorize("hasAnyRole('TECNICO','ADMINISTRADOR')")
    public String assumir(@PathVariable String id, @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes redirect) {
        return executar(id, redirect, () -> chamadoService.assumir(id, usuario), "Chamado assumido.");
    }

    /**
     * Muda o status do chamado, com comentário opcional no histórico.
     * 
     * @param id       identificador do chamado
     * @param form     status e comentário
     * @param usuario  usuário logado
     * @param redirect mensagem de sucesso ou falha
     * 
     * @return redirecionamento ao detalhe
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECNICO','ADMINISTRADOR')")
    public String alterarStatus(@PathVariable String id, @ModelAttribute AlteracaoStatusRequest form,
            @AuthenticationPrincipal Usuario usuario, RedirectAttributes redirect) {
        return executar(id, redirect,
                () -> chamadoService.mudarStatus(id, form.getStatus(), usuario, form.getComentario()),
                "Status alterado.");
    }

    /**
     * Executa uma ação sobre o chamado e devolve a mensagem adequada ao
     * detalhe.
     * 
     * @param id       identificador do chamado
     * @param redirect mensagens da tela
     * @param acao     ação a executar
     * @param sucesso  mensagem em caso de êxito
     * 
     * @return redirecionamento ao detalhe
     */
    private String executar(String id, RedirectAttributes redirect, Runnable acao, String sucesso) {
        try {
            acao.run();
            redirect.addFlashAttribute("sucesso", sucesso);
        } catch (IllegalStateException | ChamadoInvalidoException | NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chamados/" + id;
    }

    /**
     * Monta o mapa de id para nome de clientes e usuários, usado nas telas. 
     * 
     * @return nomes por id
     */
    private Map<String, String> nomes() {
        Map<String, String> nomes = new HashMap<>();
        clienteService.listarClientes().forEach(c -> nomes.put(c.getId(), c.getNome()));
        usuarioService.listarTodos()
                .forEach(u -> nomes.put(u.getId(), u.getNome() != null ? u.getNome() : u.getUsername()));
        return nomes;
    }

    /**
     * Preenche o modo, a ação e as listas do formulário compartilhado. 
     * 
     * @param model modelo da tela 
     * @param edicao verdadeiro na edição 
     * @param id identificador do chamado, nulo na abertura
     */
    private void preencherFormulario(Model model, boolean edicao, String id) {
        model.addAttribute("modoEdicao", edicao);
        model.addAttribute("acao", edicao ? "/chamados/" + id + "/editar" : "/chamados");
        model.addAttribute("voltar", edicao ? "/chamados/" + id : "/chamados");
        model.addAttribute("prioridades", Prioridade.values());
        if (!edicao) {
            model.addAttribute("clientes", clienteService.listarClientes());
        }
    }
}
