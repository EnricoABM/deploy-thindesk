package com.pfc.thindesk.horarios.dto;

import com.pfc.thindesk.horarios.HorarioAtendimento;

import lombok.Value;

/** Horário de atendimento enviado à tela. */
@Value
public class HorarioResponse {

    String id;
    String setor;
    String diaSemana;
    String horarioInicio;
    String horarioFim;

    public static HorarioResponse de(HorarioAtendimento horario) {
        return new HorarioResponse(horario.getId(), horario.getSetor(), horario.getDiaSemana(),
                horario.getHorarioInicio(), horario.getHorarioFim());
    }
}
