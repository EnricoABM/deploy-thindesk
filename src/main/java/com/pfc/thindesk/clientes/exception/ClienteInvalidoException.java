package com.pfc.thindesk.clientes.exception;

/**
 * Exceção lançada quando os dados de um cliente não passam nas validações de
 * cadastro.
 * 
 * @author Enrico Bertozzi
 */
public class ClienteInvalidoException extends IllegalArgumentException {

    /**
     * Cria a exceção com o motivo da recusa.
     * 
     * @param mensagem texto exibido ao usuário
     */
    public ClienteInvalidoException(String mensagem) {
        super(mensagem);
    }
}
