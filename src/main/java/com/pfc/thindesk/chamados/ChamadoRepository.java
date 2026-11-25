package com.pfc.thindesk.chamados;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChamadoRepository extends MongoRepository<Chamado, String> {
    List<Chamado> findByStatus(StatusChamado status);

    List<Chamado> findByAbertoPorId(String abertoPorId);

    List<Chamado> findByTecnicoId(String tecnicoId);

    List<Chamado> findByTecnicoIdIsNull();

    List<Chamado> findByStatusAndTecnicoIdIsNull(StatusChamado status);
}
