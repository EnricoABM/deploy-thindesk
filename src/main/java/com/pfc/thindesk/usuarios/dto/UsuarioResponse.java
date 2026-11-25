package com.pfc.thindesk.usuarios.dto;

import com.pfc.thindesk.usuarios.Perfil;
import com.pfc.thindesk.usuarios.Usuario;

import lombok.Value;

/**
 * DTO para exibição de dados dos usuários
 * 
 * @author Enrico Bertozzi
 */
@Value
public class UsuarioResponse {

    String id;
    String nome;
    String username;
    Perfil perfil;
    boolean ativo;
    boolean editavel;

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(),
                usuario.getPerfil(), usuario.isAtivo(), usuario.getPerfil() != Perfil.ADMINISTRADOR);
    }
}
