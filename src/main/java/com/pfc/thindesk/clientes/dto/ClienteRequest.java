package com.pfc.thindesk.clientes.dto;

import com.pfc.thindesk.clientes.Cliente;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados recebidos para criar um cliente (JSON). */
@Getter
@Setter
@NoArgsConstructor
public class ClienteRequest {

    private String nome;
    private String telefone;
    private String setor;

    public Cliente paraEntidade() {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setSetor(setor);
        return cliente;
    }
}
