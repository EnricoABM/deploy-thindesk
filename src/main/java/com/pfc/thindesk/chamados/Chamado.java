package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chamados")
@Getter
@Setter
@NoArgsConstructor
public class Chamado {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String tipo;
    private Prioridade prioridade = Prioridade.MEDIA;
    private StatusChamado status = StatusChamado.ABERTO;

    private String clienteId;
    private String abertoPorId;
    private String tecnicoId;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<EventoChamado> historico = new ArrayList<>();
}
