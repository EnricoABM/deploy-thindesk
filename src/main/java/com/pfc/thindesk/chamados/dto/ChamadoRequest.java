package com.pfc.thindesk.chamados.dto;

import com.pfc.thindesk.chamados.Chamado;
import com.pfc.thindesk.chamados.Prioridade;
import com.pfc.thindesk.chamados.StatusChamado;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos para abrir ou alterar um chamado
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

    /**
     * Cria o formulário de edição a partir de um chamado existente.
     * 
     * @param chamado chamado a editar
     * @return formulário preenchido
     */
    public static ChamadoRequest de(Chamado chamado) {
        ChamadoRequest request = new ChamadoRequest();
        request.setTitulo(chamado.getTitulo());
        request.setDescricao(chamado.getDescricao());
        request.setTipo(chamado.getTipo());
        request.setPrioridade(chamado.getPrioridade());
        return request;
    }

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
