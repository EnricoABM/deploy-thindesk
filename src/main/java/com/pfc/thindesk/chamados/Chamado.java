package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Chamado de atendimento, com o histórico de mudanças de status.
 * Os campos indexados são os usados nos filtros da lista e da dashboard.
 *
 * @author Enrico Bertozzi
 */
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
    @Indexed
    private StatusChamado status = StatusChamado.ABERTO;

    private String clienteId;
    @Indexed
    private String abertoPorId;
    @Indexed
    private String tecnicoId;

    @Indexed
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<EventoChamado> historico = new ArrayList<>();
}
