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

    /** Cria o formulário a partir de um cliente existente. @param cliente cliente a editar @return formulário preenchido */
    public static ClienteRequest de(Cliente cliente) {
        ClienteRequest request = new ClienteRequest();
        request.setNome(cliente.getNome());
        request.setTelefone(cliente.getTelefone());
        request.setSetor(cliente.getSetor());
        return request;
    }

    public Cliente paraEntidade() {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setSetor(setor);
        return cliente;
    }
}
