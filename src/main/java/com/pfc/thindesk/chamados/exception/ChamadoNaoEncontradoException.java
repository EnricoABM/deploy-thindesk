package com.pfc.thindesk.chamados.exception;

import java.util.NoSuchElementException;

/** Exceção lançada quando um chamado pedido pelo id não existe. */
public class ChamadoNaoEncontradoException extends NoSuchElementException {

    /** Cria a exceção com a mensagem padrão de chamado não encontrado. */
    public ChamadoNaoEncontradoException() {
        super("Chamado não encontrado");
    }
}
