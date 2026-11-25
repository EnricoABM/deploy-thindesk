package com.pfc.thindesk.clientes;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteRepository extends MongoRepository<Cliente, String> {
    List<Cliente> findByNome(String nome);

    List<Cliente> findBySetor(String setor);
}
