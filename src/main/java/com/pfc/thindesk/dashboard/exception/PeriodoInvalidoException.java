package com.pfc.thindesk.dashboard.exception;

/** Exceção lançada quando o período pedido para a dashboard não é uma opção válida. */
public class PeriodoInvalidoException extends IllegalArgumentException {

    /**
     * Cria a exceção com o motivo da recusa.
     *
     * @param mensagem texto exibido ao usuário
     */
    public PeriodoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
