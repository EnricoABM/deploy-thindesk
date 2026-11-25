package com.pfc.thindesk.chamados.exception;

/** Exceção lançada quando os dados de um chamado não passam nas validações. */
public class ChamadoInvalidoException extends IllegalArgumentException {

    /**
     * Cria a exceção com o motivo da recusa.
     * 
     * @param mensagem texto exibido ao usuário
     */
    public ChamadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
