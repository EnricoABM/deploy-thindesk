package com.pfc.thindesk.chamados.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.pfc.thindesk.chamados.Chamado;
import com.pfc.thindesk.chamados.Prioridade;
import com.pfc.thindesk.chamados.StatusChamado;

import lombok.Value;

/** Chamado enviado ao frontend (JSON e telas). */
@Value
public class ChamadoResponse {

    String id;
    String titulo;
    String descricao;
    String tipo;
    Prioridade prioridade;
    StatusChamado status;
    String clienteId;
    String abertoPorId;
    String tecnicoId;
    LocalDateTime criadoEm;
    LocalDateTime atualizadoEm;
    List<EventoChamadoResponse> historico;

    public static ChamadoResponse de(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getTipo(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getClienteId(),
                chamado.getAbertoPorId(),
                chamado.getTecnicoId(),
                chamado.getCriadoEm(),
                chamado.getAtualizadoEm(),
                chamado.getHistorico().stream().map(EventoChamadoResponse::de).toList());
    }
}
