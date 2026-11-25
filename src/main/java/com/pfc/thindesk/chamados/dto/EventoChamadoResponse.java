package com.pfc.thindesk.chamados.dto;

import java.time.LocalDateTime;

import com.pfc.thindesk.chamados.EventoChamado;
import com.pfc.thindesk.chamados.StatusChamado;

import lombok.Value;

/** Item do histórico de um chamado enviado ao frontend. */
@Value
public class EventoChamadoResponse {

    String autorId;
    LocalDateTime quando;
    StatusChamado statusAnterior;
    StatusChamado statusNovo;
    String comentario;

    public static EventoChamadoResponse de(EventoChamado evento) {
        return new EventoChamadoResponse(evento.getAutorId(), evento.getQuando(), evento.getStatusAnterior(),
                evento.getStatusNovo(), evento.getComentario());
    }
}
