package com.pfc.thindesk.clientes.exception;

import java.util.NoSuchElementException;

/** 
 * Exceção lançada quando um cliente pedido pelo id não existe. 
 * 
 * @author Enrico Bertozzi
 */
public class ClienteNaoEncontradoException extends NoSuchElementException {

    /** 
     * Cria a exceção com a mensagem padrão de cliente não encontrado. 
     */
    public ClienteNaoEncontradoException() {
        super("Cliente não encontrado");
    }
}
