package com.pfc.thindesk.usuarios.dto;

import com.pfc.thindesk.usuarios.Perfil;
import com.pfc.thindesk.usuarios.Usuario;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para preenchimento de formulário de cadastro ou edição
 * 
 * @author Enrico Bertozzi
 */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioForm {

    private String nome;
    private String username;
    private Perfil perfil;
    private Boolean ativo;
    private String senha;

    public static UsuarioForm novo() {
        UsuarioForm form = new UsuarioForm();
        form.setAtivo(true);
        return form;
    }

    public static UsuarioForm de(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.setNome(usuario.getNome());
        form.setUsername(usuario.getUsername());
        form.setPerfil(usuario.getPerfil());
        form.setAtivo(usuario.isAtivo());
        return form;
    }

    /** Monta a entidade para cadastro. A senha segue em texto puro até o service gerar o hash. */
    public Usuario paraEntidade() {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNome(nome);
        usuario.setPerfil(perfil);
        usuario.setPassword(senha);
        return usuario;
    }

    public boolean estaAtivo() {
        return Boolean.TRUE.equals(ativo);
    }
}
