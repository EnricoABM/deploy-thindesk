package com.pfc.thindesk.horarios;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "horariosAtendimento")
@Getter
@Setter
@NoArgsConstructor
public class HorarioAtendimento {

    @Id
    private String id;
    private String setor;
    private String diaSemana; // Exemplo: "Segunda-feira"
    private String horarioInicio; // Exemplo: "08:00"
    private String horarioFim; // Exemplo: "18:00"
}
