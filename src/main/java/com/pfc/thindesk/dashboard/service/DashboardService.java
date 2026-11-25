package com.pfc.thindesk.dashboard.service;

import static com.pfc.thindesk.chamados.StatusChamado.ABERTO;
import static com.pfc.thindesk.chamados.StatusChamado.AGUARDANDO_CLIENTE;
import static com.pfc.thindesk.chamados.StatusChamado.EM_ATENDIMENTO;
import static com.pfc.thindesk.chamados.StatusChamado.FECHADO;
import static com.pfc.thindesk.chamados.StatusChamado.RESOLVIDO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.pfc.thindesk.chamados.Chamado;
import com.pfc.thindesk.chamados.EventoChamado;
import com.pfc.thindesk.chamados.StatusChamado;
import com.pfc.thindesk.dashboard.dto.Cartao;
import com.pfc.thindesk.dashboard.dto.DashboardResposta;
import com.pfc.thindesk.dashboard.dto.LinhaStatus;
import com.pfc.thindesk.dashboard.dto.SerieDiaria;
import com.pfc.thindesk.dashboard.model.PeriodoFiltro;
import com.pfc.thindesk.dashboard.repository.DashboardRepository;
import com.pfc.thindesk.usuarios.Perfil;
import com.pfc.thindesk.usuarios.Usuario;

/**
 * Responspável por calcular os dados da dashboard
 *
 * @author Enrico Bertozzi
 */
@Service
public class DashboardService {

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private final DashboardRepository repository;
    private final ZoneId fusoHorario;

    /**
     * Cria o service com o repositório e o fuso horário configurado.
     *
     * @param repository  consultas de chamados da dashboard
     * @param fusoHorario identificador de fuso, por exemplo {@code America/Sao_Paulo}
     */
    public DashboardService(DashboardRepository repository,
            @Value("${thindesk.fuso-horario:America/Sao_Paulo}") String fusoHorario) {
        this.repository = repository;
        this.fusoHorario = ZoneId.of(fusoHorario);
    }

    /**
     * Monta a dashboard do usuário para o período escolhido. O período termina hoje
     * e começa {@code dias - 1} dias antes, de modo que a janela tem exatamente
     * {@code dias} dias corridos.
     *
     * @param usuario quem está vendo a dashboard
     * @param periodo período do filtro
     * @return cards e série diária prontos para a tela
     */
    public DashboardResposta montar(Usuario usuario, PeriodoFiltro periodo) {
        LocalDate fim = LocalDate.now(fusoHorario);
        LocalDate inicio = fim.minusDays(periodo.getDias() - 1L);

        Criteria janela = Criteria.where("criadoEm").gte(paraArmazenado(inicio));
        if (usuario.getPerfil() == Perfil.ATENDENTE) {
            janela = janela.and("abertoPorId").is(usuario.getId());
        } else if (usuario.getPerfil() == Perfil.TECNICO) {
            janela = janela.and("tecnicoId").is(usuario.getId());
        }

        List<Cartao> cartoes = cartoes(usuario.getPerfil(), janela);
        SerieDiaria serie = serie(janela, inicio, fim);
        return new DashboardResposta(periodo, inicio, fim, cartoes, serie);
    }

    /**
     * Define os cards de cada perfil.
     * <ul>
     * <li>Administrador: Abertos, Em andamento e Finalizados.</li>
     * <li>Técnico: Em aberto (qualquer status diferente de FECHADO) e Finalizados.</li>
     * <li>Atendente: Em aberto (diferente de FECHADO), Realizados (RESOLVIDO) e Fechados.</li>
     * </ul>
     *
     * @param perfil perfil do usuário
     * @param janela critério de período e escopo
     * @return cards na ordem de exibição
     */
    private List<Cartao> cartoes(Perfil perfil, Criteria janela) {
        return switch (perfil) {
            case ADMINISTRADOR -> List.of(
                    cartao("Abertos", janela, em(ABERTO)),
                    cartao("Em andamento", janela, em(EM_ATENDIMENTO, AGUARDANDO_CLIENTE, RESOLVIDO)),
                    cartao("Finalizados", janela, em(FECHADO)));
            case TECNICO -> List.of(
                    cartao("Em aberto", janela, diferenteDe(FECHADO)),
                    cartao("Finalizados", janela, em(FECHADO)));
            case ATENDENTE -> List.of(
                    cartao("Em aberto", janela, diferenteDe(FECHADO)),
                    cartao("Realizados", janela, em(RESOLVIDO)),
                    cartao("Fechados", janela, em(FECHADO)));
        };
    }

    /**
     * Conta os chamados da janela que atendem a um filtro de status.
     *
     * @param rotulo       texto do card
     * @param janela       critério de período e escopo
     * @param filtroStatus critério de status
     * @return card com a contagem
     */
    private Cartao cartao(String rotulo, Criteria janela, Criteria filtroStatus) {
        return new Cartao(rotulo, repository.contar(new Criteria().andOperator(janela, filtroStatus)));
    }

    /**
     * Monta a série diária: para cada dia do período, quantos chamados da janela
     * estavam em cada status ao fim do dia. O estado de cada chamado é reconstruído
     * a partir do histórico, começando em ABERTO na data de criação.
     *
     * @param janela critério de período e escopo
     * @param inicio primeiro dia do período
     * @param fim    último dia do período
     * @return datas e uma linha por status
     */
    private SerieDiaria serie(Criteria janela, LocalDate inicio, LocalDate fim) {
        List<LocalDate> dias = inicio.datesUntil(fim.plusDays(1)).toList();

        Map<StatusChamado, long[]> contagens = new EnumMap<>(StatusChamado.class);
        for (StatusChamado status : StatusChamado.values()) {
            contagens.put(status, new long[dias.size()]);
        }

        for (Chamado chamado : repository.buscar(janela)) {
            if (chamado.getCriadoEm() == null) {
                continue;
            }
            LocalDate diaCriacao = diaDoArmazenado(chamado.getCriadoEm());
            List<Marco> marcos = marcos(chamado, diaCriacao);

            for (int i = 0; i < dias.size(); i++) {
                LocalDate dia = dias.get(i);
                if (dia.isBefore(diaCriacao)) {
                    continue;
                }
                long[] contagem = contagens.get(statusEm(marcos, dia));
                contagem[i]++;
            }
        }

        List<String> datas = dias.stream().map(d -> d.format(DIA_MES)).toList();
        List<LinhaStatus> linhas = Arrays.stream(StatusChamado.values())
                .map(status -> new LinhaStatus(status.getRotulo(),
                        Arrays.stream(contagens.get(status)).boxed().toList()))
                .toList();
        return new SerieDiaria(datas, linhas);
    }

    /**
     * Lista as mudanças de status de um chamado em ordem cronológica, começando
     * pelo status inicial na data de criação.
     *
     * @param chamado     chamado com histórico
     * @param diaCriacao  dia em que o chamado foi criado
     * @return marcos ordenados por dia
     */
    private List<Marco> marcos(Chamado chamado, LocalDate diaCriacao) {
        List<Marco> marcos = new ArrayList<>();
        marcos.add(new Marco(diaCriacao, ABERTO));

        List<EventoChamado> eventos = new ArrayList<>(chamado.getHistorico());
        eventos.removeIf(e -> e.getQuando() == null || e.getStatusNovo() == null);
        eventos.sort(Comparator.comparing(EventoChamado::getQuando));

        for (EventoChamado evento : eventos) {
            marcos.add(new Marco(diaDoArmazenado(evento.getQuando()), evento.getStatusNovo()));
        }
        return marcos;
    }

    /**
     * Retorna o status de um chamado ao fim de um dia, a partir dos marcos.
     *
     * @param marcos marcos do chamado, ordenados por dia
     * @param dia    dia consultado
     * @return status vigente ao fim do dia
     */
    private static StatusChamado statusEm(List<Marco> marcos, LocalDate dia) {
        StatusChamado status = ABERTO;
        for (Marco marco : marcos) {
            if (marco.dia().isAfter(dia)) {
                break;
            }
            status = marco.status();
        }
        return status;
    }

    /**
     * Converte o início do período para o valor gravado no banco.
     *
     * @param dia primeiro dia do período
     * @return meia-noite do dia no fuso configurado, expressa no fuso da JVM
     */
    private LocalDateTime paraArmazenado(LocalDate dia) {
        return LocalDateTime.ofInstant(dia.atStartOfDay(fusoHorario).toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converte um valor gravado no banco para o dia correspondente no fuso configurado.
     *
     * @param valor data e hora gravada
     * @return dia no fuso configurado
     */
    private LocalDate diaDoArmazenado(LocalDateTime valor) {
        return ZonedDateTime.of(valor, ZoneId.systemDefault()).withZoneSameInstant(fusoHorario).toLocalDate();
    }

    /** Dia em que um chamado passou a ter um status. */
    private record Marco(LocalDate dia, StatusChamado status) {
    }

    /**
     * Status que agrupa os cards de "em andamento" do administrador.
     *
     * @param status lista de status
     * @return critério que aceita qualquer um deles
     */
    private static Criteria em(StatusChamado... status) {
        return Criteria.where("status").in((Object[]) status);
    }

    /**
     * Critério que aceita qualquer status diferente do informado.
     *
     * @param status status excluído
     * @return critério de status
     */
    private static Criteria diferenteDe(StatusChamado status) {
        return Criteria.where("status").ne(status);
    }
}
