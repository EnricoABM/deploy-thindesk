package com.pfc.thindesk.horarios;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface HorarioAtendimentoRepository extends MongoRepository<HorarioAtendimento, String> {
    List<HorarioAtendimento> findBySetor(String setor);
}
