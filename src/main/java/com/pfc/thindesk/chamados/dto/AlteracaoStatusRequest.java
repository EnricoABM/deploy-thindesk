package com.pfc.thindesk.chamados.dto;

import com.pfc.thindesk.chamados.StatusChamado;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados do formulário de mudança de status de um chamado. */
@Getter
@Setter
@NoArgsConstructor
public class AlteracaoStatusRequest {

    private StatusChamado status;
    private String comentario;
}
