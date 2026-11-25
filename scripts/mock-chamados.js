

const banco = db.getSiblingDB("thindesk");

const CLASSE_USUARIO = "com.pfc.thindesk.usuarios.Usuario";
const CLASSE_CLIENTE = "com.pfc.thindesk.clientes.Cliente";
const CLASSE_CHAMADO = "com.pfc.thindesk.chamados.Chamado";

// BCrypt (strength 10) de "Mock@1234", gerado com o BCryptPasswordEncoder do Spring.
const SENHA_HASH = "$2a$10$e5lNCOsPTIrgXcrIj4qZ..QZuaKcPzePnXR9iWVBfEnMAFZ5DWeAS";

const TOTAL_CHAMADOS = 50;
const DIAS_HISTORICO = 85;
const HORA = 60 * 60 * 1000;

// ---------------------------------------------------------------- usuário
const EMAIL = "atendente.mock@thindesk.com";
banco.usuarios.updateOne(
  { username: EMAIL },
  {
    $setOnInsert: {
      username: EMAIL,
      password: SENHA_HASH,
      nome: "Atendente Mock",
      perfil: "ATENDENTE",
      ativo: true,
      _class: CLASSE_USUARIO,
    },
  },
  { upsert: true }
);
const atendente = banco.usuarios.findOne({ username: EMAIL });
const atendenteId = atendente._id.toString();

// ---------------------------------------------------------------- técnico
const tecnico =
  banco.usuarios.findOne({ perfil: "TECNICO", ativo: true }) ||
  banco.usuarios.findOne({ perfil: "ADMINISTRADOR" });
const tecnicoId = tecnico ? tecnico._id.toString() : null;

// ---------------------------------------------------------------- cliente
const NOME_CLIENTE = "Cliente Mock Ltda";
banco.clientes.updateOne(
  { nome: NOME_CLIENTE },
  {
    $setOnInsert: {
      nome: NOME_CLIENTE,
      telefone: "11987654321",
      setor: "Financeiro",
      _class: CLASSE_CLIENTE,
    },
  },
  { upsert: true }
);
const clienteId = banco.clientes.findOne({ nome: NOME_CLIENTE })._id.toString();

// ---------------------------------------------------------------- catálogo
const MODELOS = [
  { tipo: "Hardware", titulo: "Computador não liga", descricao: "A estação de trabalho não liga após queda de energia." },
  { tipo: "Hardware", titulo: "Impressora atolando papel", descricao: "Impressora do setor atola papel a cada poucas folhas." },
  { tipo: "Hardware", titulo: "Monitor piscando", descricao: "Monitor apresenta cintilação intermitente durante o uso." },
  { tipo: "Hardware", titulo: "Teclado com teclas falhando", descricao: "Algumas teclas não respondem ou repetem caracteres." },
  { tipo: "Rede", titulo: "Sem acesso à internet", descricao: "Usuário sem conexão com a internet desde o início do expediente." },
  { tipo: "Rede", titulo: "Wi-Fi instável na sala de reuniões", descricao: "Conexão sem fio cai com frequência durante as reuniões." },
  { tipo: "Rede", titulo: "VPN não conecta", descricao: "Cliente VPN retorna erro de autenticação ao conectar." },
  { tipo: "Software", titulo: "Erro ao abrir o ERP", descricao: "O sistema ERP fecha sozinho logo após o login." },
  { tipo: "Software", titulo: "Instalação de pacote Office", descricao: "Solicitação de instalação do pacote Office em nova máquina." },
  { tipo: "Software", titulo: "Planilha corrompida", descricao: "Planilha de controle financeiro não abre e exibe erro de arquivo." },
  { tipo: "Software", titulo: "Atualização do antivírus", descricao: "Antivírus está desatualizado e exibe alerta de proteção." },
  { tipo: "Acesso", titulo: "Reset de senha", descricao: "Usuário bloqueado após várias tentativas de login." },
  { tipo: "Acesso", titulo: "Permissão em pasta compartilhada", descricao: "Necessário acesso de leitura e escrita à pasta do setor." },
  { tipo: "Acesso", titulo: "Criação de conta de e-mail", descricao: "Novo colaborador precisa de conta de e-mail corporativo." },
  { tipo: "E-mail", titulo: "E-mails não chegam", descricao: "Mensagens externas não estão chegando na caixa de entrada." },
  { tipo: "E-mail", titulo: "Caixa de e-mail cheia", descricao: "Caixa de correio atingiu o limite de armazenamento." },
];
const PRIORIDADES = ["BAIXA", "MEDIA", "MEDIA", "ALTA", "CRITICA"];
const MOTIVOS_CANCELAMENTO = [
  "Cliente resolveu o problema por conta própria.",
  "Chamado aberto em duplicidade.",
  "Solicitação cancelada a pedido do cliente.",
];

/**
 * Status final do chamado conforme sua idade: chamados antigos tendem a estar
 * fechados; os recentes, abertos ou em atendimento. A distribuição é
 * determinística para que o script gere sempre o mesmo resultado.
 */
function statusPara(indice, idadeDias) {
  if (indice % 9 === 4) return "CANCELADO";
  if (idadeDias > 20) return indice % 6 === 0 ? "EM_ATENDIMENTO" : "FECHADO";
  if (idadeDias > 7) return ["FECHADO", "EM_ATENDIMENTO", "FECHADO", "ABERTO"][indice % 4];
  return ["ABERTO", "EM_ATENDIMENTO", "ABERTO"][indice % 3];
}

function evento(autorId, quando, de, para, comentario) {
  return { autorId, quando, statusAnterior: de, statusNovo: para, comentario };
}

// ---------------------------------------------------------------- chamados
banco.chamados.deleteMany({ abertoPorId: atendenteId });

const agora = Date.now();
const passo = (DIAS_HISTORICO * 24 * HORA) / (TOTAL_CHAMADOS - 1);
const chamados = [];

for (let i = 0; i < TOTAL_CHAMADOS; i++) {
  // Do mais antigo ao mais recente, com variação de até ±6h para não ficar regular.
  const variacao = (((i * 7) % 13) - 6) * HORA;
  const criadoEm = new Date(agora - DIAS_HISTORICO * 24 * HORA + i * passo + variacao - HORA);
  const idadeDias = (agora - criadoEm.getTime()) / (24 * HORA);

  const modelo = MODELOS[i % MODELOS.length];
  let status = statusPara(i, idadeDias);
  if (!tecnicoId && (status === "EM_ATENDIMENTO" || status === "FECHADO")) {
    status = "ABERTO";
  }

  const historico = [];
  let atualizadoEm = criadoEm;
  let chamadoTecnicoId = null;
  const passoAtendimento = Math.min(idadeDias / 3, 1) * (2 + (i % 5)) * HORA;
  const assumidoEm = new Date(criadoEm.getTime() + passoAtendimento);

  if (status === "EM_ATENDIMENTO" || status === "FECHADO") {
    chamadoTecnicoId = tecnicoId;
    historico.push(evento(tecnicoId, assumidoEm, "ABERTO", "EM_ATENDIMENTO", "Chamado assumido"));
    atualizadoEm = assumidoEm;
  }
  if (status === "FECHADO") {
    const fechadoEm = new Date(assumidoEm.getTime() + (4 + (i % 20)) * HORA);
    historico.push(evento(tecnicoId, fechadoEm, "EM_ATENDIMENTO", "FECHADO", "Problema resolvido e validado com o cliente."));
    atualizadoEm = fechadoEm;
  }

  const chamado = {
    titulo: `${modelo.titulo} #${String(i + 1).padStart(2, "0")}`,
    descricao: modelo.descricao,
    tipo: modelo.tipo,
    prioridade: PRIORIDADES[i % PRIORIDADES.length],
    status,
    clienteId,
    abertoPorId: atendenteId,
    tecnicoId: chamadoTecnicoId,
    criadoEm,
    atualizadoEm,
    historico,
    _class: CLASSE_CHAMADO,
  };

  if (status === "CANCELADO") {
    const canceladoEm = new Date(criadoEm.getTime() + (1 + (i % 4)) * HORA);
    const motivo = MOTIVOS_CANCELAMENTO[i % MOTIVOS_CANCELAMENTO.length];
    historico.push(evento(atendenteId, canceladoEm, "ABERTO", "CANCELADO", motivo));
    chamado.atualizadoEm = canceladoEm;
    chamado.canceladoEm = canceladoEm;
    chamado.canceladoPorId = atendenteId;
    chamado.motivoCancelamento = motivo;
  }

  chamados.push(chamado);
}

banco.chamados.insertMany(chamados);

// ---------------------------------------------------------------- resumo
const porStatus = banco.chamados.aggregate([
  { $match: { abertoPorId: atendenteId } },
  { $group: { _id: "$status", total: { $sum: 1 } } },
  { $sort: { _id: 1 } },
]).toArray();

print(`Atendente: ${EMAIL} (senha Mock@1234) id=${atendenteId}`);
print(`Cliente:   ${NOME_CLIENTE} id=${clienteId}`);
print(`Técnico:   ${tecnico ? `${tecnico.username} (${tecnico.perfil})` : "nenhum — chamados ficaram ABERTO"}`);
print(`Chamados inseridos: ${chamados.length}`);
porStatus.forEach((s) => print(`  ${s._id}: ${s.total}`));
