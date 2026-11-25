package com.pfc.thindesk.usuarios;

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

import com.pfc.thindesk.usuarios.dto.UsuarioForm;
import com.pfc.thindesk.usuarios.dto.UsuarioResponse;

import lombok.RequiredArgsConstructor;

/**
 * Controller para gerenciador usuários do sistema
 * 
 * @author Enrico Bertozzi
 */
@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) String perfil,
            Model model) {
        Perfil filtro = perfilOuNulo(perfil);
        model.addAttribute("usuarios", usuarioService.filtrar(q, filtro).stream()
                .map(UsuarioResponse::de)
                .toList());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("perfilFiltro", filtro == null ? "" : filtro.name());
        model.addAttribute("perfisFiltro", Perfil.values());
        return "usuarios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", UsuarioForm.novo());
        preencherFormulario(model, false, null);
        return "usuario-form";
    }

    @PostMapping
    public String cadastrar(@ModelAttribute("form") UsuarioForm form, Model model, RedirectAttributes redirect) {
        try {
            usuarioService.cadastrarOperador(form.paraEntidade());
            redirect.addFlashAttribute("sucesso", "Usuário '" + form.getUsername() + "' cadastrado.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException | IllegalStateException e) {
            preencherFormulario(model, false, null);
            model.addAttribute("erro", e.getMessage());
            return "usuario-form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioService.buscarOperador(id);
            model.addAttribute("form", UsuarioForm.de(usuario));
            preencherFormulario(model, true, id);
            return "usuario-form";
        } catch (NoSuchElementException | IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable String id, @ModelAttribute("form") UsuarioForm form,
            Model model, RedirectAttributes redirect) {
        try {
            usuarioService.atualizarOperador(id, form.getUsername(), form.getNome(), form.getPerfil(),
                    form.estaAtivo(), form.getSenha());
            redirect.addFlashAttribute("sucesso", "Usuário '" + form.getUsername() + "' atualizado.");
            return "redirect:/usuarios";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        } catch (IllegalArgumentException | IllegalStateException e) {
            preencherFormulario(model, true, id);
            model.addAttribute("erro", e.getMessage());
            return "usuario-form";
        }
    }

    /** Preenche o formulário compartilhado entre cadastro e edição. */
    private void preencherFormulario(Model model, boolean edicao, String id) {
        model.addAttribute("modoEdicao", edicao);
        model.addAttribute("usuarioId", id);
        model.addAttribute("acao", edicao ? "/usuarios/" + id + "/editar" : "/usuarios");
        model.addAttribute("perfis", new Perfil[] { Perfil.ATENDENTE, Perfil.TECNICO });
    }

    private static Perfil perfilOuNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Perfil.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
