package com.pfc.thindesk.chamados;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.pfc.thindesk.chamados.exception.ChamadoInvalidoException;
import com.pfc.thindesk.chamados.exception.ChamadoNaoEncontradoException;
import com.pfc.thindesk.clientes.ClienteService;
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
    private final ClienteService clienteService;

    /**
     * Valida e abre um chamado em status ABERTO.
     * 
     * @param chamado dados do chamado
     * @param autor   usuário que abre o chamado
     * 
     * @return chamado salvo
     */
    public Chamado criarChamado(Chamado chamado, Usuario autor) {
        if (autor.getPerfil() == Perfil.TECNICO) {
            throw new AccessDeniedException("Técnico não pode abrir chamados");
        }
        validarCamposTexto(chamado);
        if (chamado.getClienteId() == null || chamado.getClienteId().isBlank()) {
            throw new ChamadoInvalidoException("Cliente é obrigatório");
        }
        clienteService.buscarCliente(chamado.getClienteId());

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

    /** 
     * Lista apenas os chamados que o usuário pode ver, conforme o perfil. 
     */
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
        Chamado chamado = buscarPorId(id);

        if (!podeAlterar(chamado, dados, usuario)) {
            throw new AccessDeniedException("Sem permissão para alterar este chamado");
        }
        if (chamado.getTecnicoId() == null && dados.getTecnicoId() == null) {
            throw new AccessDeniedException("Chamado precisa ser assumido antes de ser alterado");
        }
        if (alteraCampos(chamado, dados)) {
            if (!podeEditar(chamado, usuario)) {
                throw new AccessDeniedException("Apenas o atendente que abriu o chamado ou um administrador pode editar seus dados");
            }
            validarCamposTexto(dados);
            chamado.setTitulo(dados.getTitulo());
            chamado.setDescricao(dados.getDescricao());
            chamado.setTipo(dados.getTipo());
            if (dados.getPrioridade() != null) {
                chamado.setPrioridade(dados.getPrioridade());
            }
        }

        if (dados.getTecnicoId() != null && !dados.getTecnicoId().equals(chamado.getTecnicoId())) {
            atribuirTecnico(chamado, dados.getTecnicoId());
        }

        if (dados.getStatus() != null) {
            alterarStatus(chamado, dados.getStatus(), usuario.getId(), "Atualização de status");
        }

        return chamadoRepository.save(chamado);
    }
     
    /** 
     * Lista os chamados visíveis ao usuário, filtrados por trecho do título e status, do mais novo ao mais antigo. 
     * 
     * @param usuario usuário logado 
     * @param titulo trecho do título, nulo ou vazio não filtra 
     * @param status status escolhido, nulo não filtra 
     * 
     * @return chamados filtrados 
     */
    public List<Chamado> filtrar(Usuario usuario, String titulo, StatusChamado status) {
        String termo = titulo == null ? "" : titulo.trim().toLowerCase(Locale.ROOT);
        return listarVisiveis(usuario).stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> termo.isEmpty()
                        || (c.getTitulo() != null && c.getTitulo().toLowerCase(Locale.ROOT).contains(termo)))
                .sorted(Comparator.comparing(Chamado::getCriadoEm,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    /** 
     * Busca um chamado que o usuário pode ver. 
     * 
     * @param id identificador do chamado 
     * @param usuario usuário logado 
     * 
     * @return chamado encontrado 
     */
    public Chamado buscarVisivel(String id, Usuario usuario) {
        Chamado chamado = buscarPorId(id);
        if (!podeVer(chamado, usuario)) {
            throw new AccessDeniedException("Sem permissão para ver este chamado");
        }
        return chamado;
    }
     
    /** 
     * Busca um chamado que o usuário pode editar e mudar o status. 
     * 
     * @param id identificador do chamado 
     * @param usuario usuário logado 
     * 
     * @return chamado encontrado 
     */
    public Chamado buscarGerenciavel(String id, Usuario usuario) {
        Chamado chamado = buscarVisivel(id, usuario);
        if (!podeGerenciar(chamado, usuario)) {
            throw new AccessDeniedException("Sem permissão para alterar este chamado");
        }
        return chamado;
    }

    /** Busca um chamado cujos dados o usuário pode editar. 
     * 
     * @param id identificador do chamado 
     * @param usuario usuário logado 
     * 
     * @return chamado encontrado */
    public Chamado buscarEditavel(String id, Usuario usuario) {
        Chamado chamado = buscarVisivel(id, usuario);
        if (chamado.getTecnicoId() == null) {
            throw new AccessDeniedException("Chamado precisa ser assumido antes de ser alterado");
        }
        if (!podeEditar(chamado, usuario)) {
            throw new AccessDeniedException("Apenas o atendente que abriu o chamado ou um administrador pode editá-lo");
        }
        return chamado;
    }

    /** 
     * Indica se o usuário pode editar título, descrição, tipo e prioridade. Exige chamado assumido. 
     * 
     * @param chamado chamado consultado 
     * @param usuario usuário logado 
     * @return verdadeiro se pode editar 
     */
    public boolean podeEditar(Chamado chamado, Usuario usuario) {
        if (chamado.getTecnicoId() == null) {
            return false;
        }
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> true;
            case ATENDENTE -> usuario.getId().equals(chamado.getAbertoPorId());
            case TECNICO -> false;
        };
    }

    /** 
     * Indica se o pedido traz valor diferente para título, descrição, tipo ou prioridade. 
     * 
     * @param chamado chamado atual 
     * @param dados valores pedidos, nulos não contam 
     * 
     * @return verdadeiro se algum campo muda */
    private static boolean alteraCampos(Chamado chamado, Chamado dados) {
        return (dados.getTitulo() != null && !dados.getTitulo().equals(chamado.getTitulo()))
                || (dados.getDescricao() != null && !dados.getDescricao().equals(chamado.getDescricao()))
                || (dados.getTipo() != null && !dados.getTipo().equals(chamado.getTipo()))
                || (dados.getPrioridade() != null && dados.getPrioridade() != chamado.getPrioridade());
    }

     
    /** 
     * Atribui o chamado ao usuário e, se estiver aberto, passa para EM_ATENDIMENTO. 
     * 
     * @param id identificador do chamado 
     * @param usuario técnico ou administrador que assume 
     * 
     * @return chamado salvo 
     */
    public Chamado assumir(String id, Usuario usuario) {
        Chamado chamado = buscarVisivel(id, usuario);
        if (!podeAssumir(chamado, usuario)) {
            if (usuario.getPerfil() == Perfil.ATENDENTE) {
                throw new AccessDeniedException("Atendente não pode assumir chamados");
            }
            throw new IllegalStateException("Chamado já possui técnico");
        }

        atribuirTecnico(chamado, usuario.getId());
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            alterarStatus(chamado, StatusChamado.EM_ATENDIMENTO, usuario.getId(), "Chamado assumido");
        }
        return chamadoRepository.save(chamado);
    }
     
    /** 
     * Muda o status se a transição for válida e registra o comentário no histórico. 
     * 
     * @param id identificador do chamado 
     * @param novo novo status 
     * @param usuario usuário logado 
     * @param comentario motivo da mudança, nulo usa texto padrão 
     * 
     * @return chamado salvo 
     */
    public Chamado mudarStatus(String id, StatusChamado novo, Usuario usuario, String comentario) {
        Chamado chamado = buscarGerenciavel(id, usuario);
        if (novo == null) {
            throw new ChamadoInvalidoException("Status é obrigatório");
        }
        String motivo = comentario == null || comentario.isBlank() ? "Atualização de status" : comentario.trim();
        alterarStatus(chamado, novo, usuario.getId(), motivo);
        return chamadoRepository.save(chamado);
    }


    /** 
     * Lista os status para os quais o chamado pode ir a partir do atual. 
     * 
     * @param atual status atual do chamado 
     * 
     * @return status permitidos */
    public List<StatusChamado> proximosStatus(StatusChamado atual) {
        return Arrays.stream(StatusChamado.values()).filter(s -> podeIr(atual, s)).toList();
    }
     
    /** 
     * Indica se o usuário pode editar o chamado e mudar seu status. 
     * 
     * @param chamado chamado consultado 
     * @param usuario usuário logado 
     * 
     * @return verdadeiro se pode gerenciar 
     */
    public boolean podeGerenciar(Chamado chamado, Usuario usuario) {
        if (chamado.getTecnicoId() == null) {
            return false;
        }
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> true;
            case TECNICO -> usuario.getId().equals(chamado.getTecnicoId());
            case ATENDENTE -> false;
        };
    }
     
    /** 
     * Indica se o chamado está sem técnico e o usuário pode assumi-lo. 
     * 
     * @param chamado chamado consultado 
     * @param usuario usuário logado 
     * 
     * @return verdadeiro se pode assumir 
     */
    public boolean podeAssumir(Chamado chamado, Usuario usuario) {
        return usuario.getPerfil() != Perfil.ATENDENTE && chamado.getTecnicoId() == null;
    }

     
    /** 
     * Busca um chamado pelo id ou falha se não existir. 
     * 
     * @param id identificador do chamado 
     * 
     * @return chamado encontrado 
     */
    private Chamado buscarPorId(String id) {
        return chamadoRepository.findById(id).orElseThrow(ChamadoNaoEncontradoException::new);
    }
     
    /** 
     * Indica se o chamado aparece para o usuário, conforme o perfil. 
     * 
     * @param chamado chamado consultado 
     * @param usuario usuário logado 
     * 
     * @return verdadeiro se pode ver 
     */
    private boolean podeVer(Chamado chamado, Usuario usuario) {
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> true;
            case ATENDENTE -> usuario.getId().equals(chamado.getAbertoPorId());
            case TECNICO -> chamado.getTecnicoId() == null || usuario.getId().equals(chamado.getTecnicoId());
        };
    }

    /** 
     * Remove espaços das bordas e valida título, descrição e tipo. 
     * 
     * @param chamado chamado com os campos a validar 
     */
    private void validarCamposTexto(Chamado chamado) {
        chamado.setTitulo(texto(chamado.getTitulo(), "Título", 3, 100));
        chamado.setDescricao(texto(chamado.getDescricao(), "Descrição", 5, 1000));
        chamado.setTipo(texto(chamado.getTipo(), "Tipo", 2, 60));
    }
     
    /** Remove espaços das bordas e confere o tamanho de um campo de texto. 
     * 
     * @param valor texto recebido 
     * @param campo nome do campo na mensagem de erro 
     * @param minimo tamanho mínimo 
     * @param maximo tamanho máximo 
     * 
     * @return texto sem espaços das bordas 
     */
    private static String texto(String valor, String campo, int minimo, int maximo) {
        String limpo = valor == null ? "" : valor.trim();
        if (limpo.length() < minimo || limpo.length() > maximo) {
            throw new ChamadoInvalidoException(campo + " deve ter entre " + minimo + " e " + maximo + " caracteres");
        }
        return limpo;
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
     * 
     * Técnico só altera chamado que já é dele ou que está sem técnico e ele 
     * stá assumindo.
     * Além disso, só pode atribuir a si mesmo. Administrador altera qualquer chamado.
     */
    private boolean podeAlterar(Chamado chamado, Chamado dados, Usuario usuario) {
        return switch (usuario.getPerfil()) {
            case ADMINISTRADOR -> true;
            case ATENDENTE -> podeEditar(chamado, usuario) && dados.getStatus() == null && dados.getTecnicoId() == null;
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
