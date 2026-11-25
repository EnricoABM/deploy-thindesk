package com.pfc.thindesk.dashboard.service;

import static com.pfc.thindesk.chamados.StatusChamado.ABERTO;
import static com.pfc.thindesk.chamados.StatusChamado.EM_ATENDIMENTO;
import static com.pfc.thindesk.chamados.StatusChamado.FECHADO;

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
 * Calcula os dados da dashboard: os cards de situação e a evolução diária por
 * status. Os dois usam o mesmo conjunto de chamados, isto é, os criados dentro
 * do período e visíveis ao usuário.
 * <p>
 * Administrador vê todos os chamados; atendente vê os que abriu; técnico vê os
 * que estão atribuídos a ele.
 * <p>
 * Datas de negócio (início do período, dia de cada evento) são calculadas no
 * fuso definido em {@code thindesk.fuso-horario}. Os valores gravados pelo Spring
 * como {@link LocalDateTime} são interpretados no fuso padrão da JVM, que é como
 * o Spring os converte ao gravar e ao ler.
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
     *
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

        return new DashboardResposta(periodo, inicio, fim, cartoes(janela), serie(janela, inicio, fim));
    }

    /**
     * Define os três cards, iguais para todos os perfis: Em aberto (ABERTO),
     * Em atendimento (EM_ATENDIMENTO) e Fechados (FECHADO). Chamados cancelados
     * não entram em nenhum card.
     *
     * @param janela critério de período e escopo
     *
     * @return cards na ordem de exibição
     */
    private List<Cartao> cartoes(Criteria janela) {
        return List.of(
                cartao("Em aberto", janela, ABERTO),
                cartao("Em atendimento", janela, EM_ATENDIMENTO),
                cartao("Fechados", janela, FECHADO));
    }

    /**
     * Conta os chamados da janela em um status.
     *
     * @param rotulo texto do card
     * @param janela critério de período e escopo
     * @param status status contado
     *
     * @return card com a contagem
     */
    private Cartao cartao(String rotulo, Criteria janela, StatusChamado status) {
        long valor = repository.contar(new Criteria().andOperator(janela, Criteria.where("status").is(status)));
        return new Cartao(rotulo, valor);
    }

    /**
     * Monta a série diária: para cada dia do período, quantos chamados da janela
     * estavam em cada status ao fim do dia. O estado de cada chamado é reconstruído
     * a partir do histórico, começando em ABERTO na data de criação.
     *
     * @param janela critério de período e escopo
     * @param inicio primeiro dia do período
     * @param fim    último dia do período
     *
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
                contagens.get(statusEm(marcos, dia))[i]++;
            }
        }

        List<String> datas = dias.stream().map(d -> d.format(DIA_MES)).toList();
        List<LinhaStatus> linhas = Arrays.stream(StatusChamado.values())
                .map(status -> new LinhaStatus(status.name(), status.getRotulo(),
                        Arrays.stream(contagens.get(status)).boxed().toList()))
                .toList();
        return new SerieDiaria(datas, linhas);
    }

    /**
     * Lista as mudanças de status de um chamado em ordem cronológica, começando
     * pelo status inicial na data de criação.
     *
     * @param chamado    chamado com histórico
     * @param diaCriacao dia em que o chamado foi criado
     *
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
     *
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
     *
     * @return meia-noite do dia no fuso configurado, expressa no fuso da JVM
     */
    private LocalDateTime paraArmazenado(LocalDate dia) {
        return LocalDateTime.ofInstant(dia.atStartOfDay(fusoHorario).toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converte um valor gravado no banco para o dia correspondente no fuso configurado.
     *
     * @param valor data e hora gravada
     *
     * @return dia no fuso configurado
     */
    private LocalDate diaDoArmazenado(LocalDateTime valor) {
        return ZonedDateTime.of(valor, ZoneId.systemDefault()).withZoneSameInstant(fusoHorario).toLocalDate();
    }

    /**
     * Dia em que um chamado passou a ter um status.
     *
     * @param dia    dia da mudança
     * @param status status a partir desse dia
     */
    private record Marco(LocalDate dia, StatusChamado status) {
    }
}
