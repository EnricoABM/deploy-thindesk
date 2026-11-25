package com.pfc.thindesk.clientes.dto;

import com.pfc.thindesk.clientes.Cliente;

import lombok.Value;

/** Dados de um cliente enviados ao frontend (JSON e telas). */
@Value
public class ClienteResponse {

    String id;
    String nome;
    String telefone;
    String setor;

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getTelefone(), cliente.getSetor());
    }
}
