package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.pfc.thindesk.usuarios.Perfil;
import com.pfc.thindesk.usuarios.Usuario;

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

    public Chamado criarChamado(Chamado chamado, Usuario autor) {
        if (autor.getPerfil() == Perfil.TECNICO) {
            throw new AccessDeniedException("Técnico não pode abrir chamados");
        }

        LocalDateTime agora = LocalDateTime.now();
        chamado.setAbertoPorId(autor.getId());
        chamado.setTecnicoId(null);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setCriadoEm(agora);
        chamado.setAtualizadoEm(agora);

        if (chamado.getPrioridade() == null) {
            chamado.setPrioridade(Prioridade.MEDIA);
        }

        return chamadoRepository.save(chamado);
    }

    /** Lista apenas os chamados que o usuário pode ver, conforme o perfil. */
    public List<Chamado> listarVisiveis(Usuario usuario) {
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> chamadoRepository.findAll();
            case ATENDENTE -> chamadoRepository.findByAbertoPorId(usuario.getId());
            case TECNICO -> {
                List<Chamado> visiveis = new ArrayList<>(chamadoRepository.findByTecnicoIdIsNull());
                visiveis.addAll(chamadoRepository.findByTecnicoId(usuario.getId()));
                yield visiveis;
            }
        };
    }

    public Chamado atualizarChamado(String id, Chamado dados, Usuario usuario) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chamado não encontrado com id: " + id));

        if (!podeAlterar(chamado, dados, usuario)) {
            throw new AccessDeniedException("Sem permissão para alterar este chamado");
        }

        chamado.setTitulo(dados.getTitulo());
        chamado.setDescricao(dados.getDescricao());
        chamado.setTipo(dados.getTipo());
        if (dados.getPrioridade() != null) {
            chamado.setPrioridade(dados.getPrioridade());
        }

        if (dados.getTecnicoId() != null && !dados.getTecnicoId().equals(chamado.getTecnicoId())) {
            atribuirTecnico(chamado, dados.getTecnicoId());
        }

        if (dados.getStatus() != null) {
            alterarStatus(chamado, dados.getStatus(), usuario.getId(), "Atualização de status");
        }

        return chamadoRepository.save(chamado);
    }

    /** Aplica a mudança de status somente se a transição for permitida e registra no histórico. */
    private void alterarStatus(Chamado chamado, StatusChamado novo, String autorId, String comentario) {
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

    private void atribuirTecnico(Chamado chamado, String tecnicoId) {
        chamado.setTecnicoId(tecnicoId);
        chamado.setAtualizadoEm(LocalDateTime.now());
    }

    /**
     * Técnico só altera chamado que já é dele ou que está sem técnico e ele está assumindo.
     * Além disso, só pode atribuir a si mesmo. Administrador altera qualquer chamado.
     */
    private boolean podeAlterar(Chamado chamado, Chamado dados, Usuario usuario) {
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> true;
            case ATENDENTE -> false;
            case TECNICO -> {
                boolean eDoTecnico = usuario.getId().equals(chamado.getTecnicoId());
                boolean assumindo = chamado.getTecnicoId() == null
                        && usuario.getId().equals(dados.getTecnicoId());
                boolean atribuiSomenteASi = dados.getTecnicoId() == null
                        || dados.getTecnicoId().equals(usuario.getId());
                yield (eDoTecnico || assumindo) && atribuiSomenteASi;
            }
        };
    }

    /** Matriz de transições permitidas entre status. */
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
