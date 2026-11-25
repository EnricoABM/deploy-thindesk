package com.pfc.thindesk.horarios.dto;

import com.pfc.thindesk.horarios.HorarioAtendimento;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados do formulário de horário de atendimento (POST). */
@Getter
@Setter
@NoArgsConstructor
public class HorarioForm {

    private String setor;
    private String diaSemana;
    private String horarioInicio;
    private String horarioFim;

    public HorarioAtendimento paraEntidade() {
        HorarioAtendimento horario = new HorarioAtendimento();
        horario.setSetor(setor);
        horario.setDiaSemana(diaSemana);
        horario.setHorarioInicio(horarioInicio);
        horario.setHorarioFim(horarioFim);
        return horario;
    }
}
