package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro embutido no histórico do chamado (mudança de status). */
@Getter
@Setter
@NoArgsConstructor
public class EventoChamado {

    private String autorId;
    private LocalDateTime quando;
    private StatusChamado statusAnterior;
    private StatusChamado statusNovo;
    private String comentario;
}
