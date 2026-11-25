package com.pfc.thindesk.chamados.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados recebidos para cancelar um chamado: a justificativa obrigatória.
 *
 * @author Enrico Bertozzi
 */
@Getter
@Setter
@NoArgsConstructor
public class CancelamentoRequest {

    private String motivo;
}
