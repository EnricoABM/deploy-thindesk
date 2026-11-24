package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service para gerenciamento de chamados do sistema
 * 
 * @author Enrico Bertozzi
 */
@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;

    /**
     * Cria novos chamados no sistema
     * 
     * @param chamado Objeto com dados para cadastro
     * @return Retorna o objeto criado
     */
    public Chamado criarChamado(Chamado chamado) {
        LocalDateTime agora = LocalDateTime.now();

        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setCriadoEm(agora);
        chamado.setAtualizadoEm(agora);

        if (chamado.getPrioridade() == null) {
            chamado.setPrioridade(Prioridade.MEDIA);
        }

        return chamadoRepository.save(chamado);
    }

    /**
     * Lista os chamados registrados
     * 
     * @return {@link List} com chamados
     */
    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    /**
     * Atualiza os dados de um chamado
     * 
     * @param id                Identificador do Chamado
     * @param chamadoAtualizado Objeto com dados do chamado
     * @return {@link Chamado} com dados alterados
     */
    public Chamado atualizarChamado(String id, Chamado chamadoAtualizado) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com id: " + id));

        chamado.setTitulo(chamadoAtualizado.getTitulo());
        chamado.setDescricao(chamadoAtualizado.getDescricao());
        chamado.setTipo(chamadoAtualizado.getTipo());
        chamado.setPrioridade(chamadoAtualizado.getPrioridade());

        if (chamadoAtualizado.getTecnicoId() != null) {
            atribuirTecnico(chamado, chamadoAtualizado.getTecnicoId());
        }

        alterarStatus(chamado, chamadoAtualizado.getStatus(), null, "Atualização de status");
        return chamadoRepository.save(chamado);
    }

    public void alterarStatus(Chamado chamado, StatusChamado novo, String autorId, String comentario) {
        if (novo == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        if (novo == chamado.getStatus()) {
            return;
        }
        if (!podeIr(chamado.getStatus(), novo)) {
            throw new IllegalStateException(
                    "Transição de status inválida: " + chamado.getStatus() + " -> " + novo);
        }

        LocalDateTime agora = LocalDateTime.now();

        EventoChamado evento = new EventoChamado();
        evento.setAutorId(autorId);
        evento.setQuando(agora);
        evento.setStatusAnterior(chamado.getStatus());
        evento.setStatusNovo(novo);
        evento.setComentario(comentario);

        chamado.getHistorico().add(evento);
        chamado.setStatus(novo);
        chamado.setAtualizadoEm(agora);
    }

    public void atribuirTecnico(Chamado chamado, String tecnicoId) {
        chamado.setTecnicoId(tecnicoId);
        chamado.setAtualizadoEm(LocalDateTime.now());
    }

    /**
     * Verifica se a transição de status é permitida
     * 
     * @param de   Estado original
     * @param para Estado destino
     * @return {@link Boolean} se a alteração é válida.
     */
    private boolean podeIr(StatusChamado de, StatusChamado para) {
        return switch (de) {
            case ABERTO -> para == StatusChamado.EM_ATENDIMENTO;
            case EM_ATENDIMENTO -> para == StatusChamado.AGUARDANDO_CLIENTE || para == StatusChamado.RESOLVIDO;
            case AGUARDANDO_CLIENTE -> para == StatusChamado.EM_ATENDIMENTO || para == StatusChamado.RESOLVIDO;
            case RESOLVIDO -> para == StatusChamado.FECHADO || para == StatusChamado.EM_ATENDIMENTO;
            case FECHADO -> false;
        };
    }
}
