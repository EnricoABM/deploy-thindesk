package com.pfc.thindesk.usuarios.exception;

/**
 * Exceção para erro em busca de usuários
 * 
 * @author Enrico Bertozzi 
 */
public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado");
    }
}
