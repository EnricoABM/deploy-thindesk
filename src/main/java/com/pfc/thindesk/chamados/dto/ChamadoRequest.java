package com.pfc.thindesk.chamados.dto;

import com.pfc.thindesk.chamados.Chamado;
import com.pfc.thindesk.chamados.Prioridade;
import com.pfc.thindesk.chamados.StatusChamado;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos para abrir ou alterar um chamado (JSON). Campos nulos não sobrescrevem
 * valores existentes na alteração; na abertura, a prioridade nula vira MÉDIA no service.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChamadoRequest {

    private String titulo;
    private String descricao;
    private String tipo;
    private Prioridade prioridade;
    private StatusChamado status;
    private String clienteId;
    private String tecnicoId;

    public Chamado paraEntidade() {
        Chamado chamado = new Chamado();
        chamado.setTitulo(titulo);
        chamado.setDescricao(descricao);
        chamado.setTipo(tipo);
        chamado.setPrioridade(prioridade);
        chamado.setStatus(status);
        chamado.setClienteId(clienteId);
        chamado.setTecnicoId(tecnicoId);
        return chamado;
    }
}
