package com.pfc.thindesk.usuarios.exception;

/** Exceção lançada quando a exclusão de um usuário é recusada por regra de negócio. */
public class UsuarioNaoExcluivelException extends IllegalStateException {

    /**
     * Cria a exceção com o motivo da recusa.
     * 
     * @param mensagem texto exibido ao usuário
     */
    public UsuarioNaoExcluivelException(String mensagem) {
        super(mensagem);
    }
}
