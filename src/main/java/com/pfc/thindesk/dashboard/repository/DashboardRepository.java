package com.pfc.thindesk.dashboard.repository;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.pfc.thindesk.chamados.Chamado;

import lombok.RequiredArgsConstructor;

/**
 * Consultas da dashboard sobre a coleção de chamados. Recebe os critérios já
 * montados pelo service, assim o banco filtra por período e escopo, sem carregar
 * chamados fora da janela.
 */
@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final MongoTemplate mongoTemplate;

    /**
     * Conta os chamados que atendem ao critério.
     *
     * @param criterio critério de busca
     * @return quantidade de chamados
     */
    public long contar(Criteria criterio) {
        return mongoTemplate.count(new Query(criterio), Chamado.class);
    }

    /**
     * Busca os chamados que atendem ao critério, com o histórico de status.
     *
     * @param criterio critério de busca
     * @return chamados encontrados
     */
    public List<Chamado> buscar(Criteria criterio) {
        return mongoTemplate.find(new Query(criterio), Chamado.class);
    }
}
