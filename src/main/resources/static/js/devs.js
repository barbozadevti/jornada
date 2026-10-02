import { post } from './api.js';
import { abrirJanela, aviso, barra, campo, chip, chipSituacao, chipTipo, dataLonga, fecharJanela, h, icone, iniciais, numero } from './ui.js';

// ---------- lista ----------

export function listaDeDevs(ctx, selecionadoId) {
  const { devs } = ctx.dados;
  const lateral = h('div', { class: 'lista-devs' }, devs.map((d) =>
    h('a', { class: 'dev-linha' + (d.id === selecionadoId ? ' ativo' : ''), href: '#/devs/' + d.id, 'aria-current': d.id === selecionadoId ? 'page' : null },
      h('span', { class: 'avatar' }, iniciais(d.nome)),
      h('span', { class: 'dev-texto' }, h('strong', {}, d.nome), h('span', { class: 'mudo' }, d.nivel.nome + ' · ' + numero(d.xp) + ' XP')),
      d.certificados ? h('span', { class: 'mini ouro', title: d.certificados + ' certificado(s)' }, icone('trofeu', 14), d.certificados) : null)));

  const dev = devs.find((d) => d.id === selecionadoId);
  if (!ctx.coordenador) {
    return h('section', {},
      h('div', { class: 'cabecalho' }, h('div', {}, h('h1', {}, 'Minha jornada'),
        h('p', { class: 'sub' }, 'Seu progresso, seu XP e o próximo passo de cada bootcamp.'))),
      dev ? painelDoDev(ctx, dev) : h('p', { class: 'vazio' }, 'Não encontrei a sua jornada.'));
  }
  return h('section', {},
    h('div', { class: 'cabecalho' },
      h('div', {}, h('h1', {}, 'Devs'), h('p', { class: 'sub' }, 'Escolha alguém para acompanhar a jornada, concluir conteúdos e acumular XP.')),
      h('button', { class: 'botao primario', type: 'button', onclick: () => janelaNovoDev(ctx) }, icone('mais'), 'Novo dev')),
    h('div', { class: 'devs-layout' + (dev ? ' com-selecao' : '') },
      lateral,
      dev ? painelDoDev(ctx, dev) : h('div', { class: 'cartao vazio-grande' }, h('p', {}, 'Selecione um dev ao lado.'))));
}

// ---------- painel do dev ----------

function painelDoDev(ctx, d) {
  const proximo = d.nivel.proximoNome
    ? 'Faltam ' + numero(d.nivel.proximoXp - d.xp) + ' XP para ' + d.nivel.proximoNome
    : 'Nível máximo';
  const matriculados = new Set(d.matriculas.map((m) => m.bootcampId));
  const livres = ctx.dados.bootcamps.filter((b) => !matriculados.has(b.id) && b.situacao !== 'ENCERRADO' && b.conteudos.length);

  return h('div', { class: 'painel-dev' },
    ctx.coordenador ? h('a', { class: 'voltar so-celular', href: '#/devs' }, icone('voltar', 16), 'Todos os devs') : null,
    h('div', { class: 'cartao dev-topo' },
      h('span', { class: 'avatar grande' }, iniciais(d.nome)),
      h('div', { class: 'dev-info' },
        h('h2', {}, d.nome),
        h('div', { class: 'linha-chips' }, chip(d.nivel.nome, 'nivel nivel-' + d.nivel.codigo.toLowerCase()), d.certificados ? chip(d.certificados + (d.certificados > 1 ? ' certificados' : ' certificado'), 'ouro') : null),
        h('div', { class: 'nivel-barra' }, barra(d.nivel.percentual, 'Progresso até o próximo nível'), h('span', { class: 'mudo' }, proximo))),
      h('div', { class: 'xp-grande', id: 'xp-do-dev' }, h('strong', {}, numero(d.xp)), h('span', {}, 'XP'))),
    d.matriculas.length
      ? d.matriculas.map((m) => cartaoDeMatricula(ctx, d, m))
      : h('div', { class: 'cartao vazio-grande' }, h('p', {}, d.nome + ' ainda não está em nenhum bootcamp.')),
    livres.length ? painelNovaMatricula(ctx, d, livres) : null);
}

function cartaoDeMatricula(ctx, d, m) {
  const passos = h('ol', { class: 'trilha compacta' }, m.trilha.map((p) =>
    h('li', { class: 'passo estado-' + p.estado.toLowerCase() + ' tipo-' + p.conteudo.tipo.toLowerCase() },
      h('span', { class: 'passo-numero' }, p.estado === 'CONCLUIDO' ? icone('check', 14) : p.estado === 'PROXIMO' ? '●' : ''),
      h('div', { class: 'passo-corpo' },
        h('div', { class: 'linha' }, h('span', { class: 'passo-titulo' }, p.conteudo.titulo), h('span', { class: 'xp', title: p.conteudo.explicacaoXp }, '+' + p.conteudo.xp + ' XP')),
        h('div', { class: 'passo-meta' }, chipTipo(p.conteudo.tipo, p.conteudo.tipoRotulo), h('span', { class: 'mudo' }, p.conteudo.detalhe),
          p.concluidoEm ? h('span', { class: 'mudo' }, '· concluído em ' + dataLonga(p.concluidoEm)) : null)))));

  let acao;
  if (m.certificado) {
    acao = h('div', { class: 'concluida' },
      h('p', {}, icone('trofeu', 18), ' Trilha concluída!'),
      h('button', { class: 'botao ouro-botao', type: 'button', onclick: () => janelaCertificado(m.certificado) }, 'Ver certificado'));
  } else if (m.proximo) {
    const botao = h('button', { class: 'botao primario grande', type: 'button', disabled: !m.podeProgredir, onclick: () => progredir(ctx, d, m, botao) },
      m.podeProgredir ? 'Concluir: ' + m.proximo.titulo + ' (+' + m.proximo.xp + ' XP)' : 'Aguardando: ' + m.proximo.titulo);
    acao = h('div', {}, botao, m.impedimento ? h('p', { class: 'impedimento' }, icone('cadeado', 14), ' ' + m.impedimento) : null);
  }

  return h('div', { class: 'cartao matricula' },
    h('div', { class: 'linha' },
      h('h3', {}, h('a', { href: '#/bootcamps/' + m.bootcampId }, m.bootcampNome)),
      chipSituacao(m.situacaoBootcamp, { PROXIMO: 'Começa em breve', EM_ANDAMENTO: 'Em andamento', ENCERRADO: 'Encerrado' }[m.situacaoBootcamp])),
    h('div', { class: 'progresso-linha' }, barra(m.percentual, 'Progresso no bootcamp'),
      h('span', {}, m.concluidos + ' de ' + m.total + ' · ' + m.percentual + '%')),
    h('p', { class: 'mudo' }, numero(m.xpGanho) + ' de ' + numero(m.xpTotal) + ' XP deste bootcamp'),
    passos, acao);
}

async function progredir(ctx, d, m, botao) {
  botao.disabled = true;
  try {
    const r = await post('/api/devs/' + d.id + '/matriculas/' + m.bootcampId + '/progresso');
    aviso('+' + r.xpGanho + ' XP · ' + r.concluido.titulo);
    await ctx.recarregar();
    const marcador = document.getElementById('xp-do-dev');
    if (marcador) { marcador.classList.add('pulou'); }
    if (r.certificadoEmitido) {
      const feita = r.dev.matriculas.find((x) => x.bootcampId === m.bootcampId);
      janelaCertificado(feita.certificado);
    }
  } catch (e) {
    aviso(e.message, 'erro');
    botao.disabled = false;
  }
}

function painelNovaMatricula(ctx, d, livres) {
  const seletor = h('select', {}, livres.map((b) => h('option', { value: b.id }, b.nome + ' (' + b.situacaoRotulo.toLowerCase() + ')')));
  return h('div', { class: 'cartao' },
    h('h3', {}, 'Matricular em outro bootcamp'),
    h('div', { class: 'linha-campos' }, campo('Bootcamp', seletor),
      h('button', { class: 'botao', type: 'button', onclick: async () => {
        try { await post('/api/devs/' + d.id + '/matriculas', { bootcampId: seletor.value }); aviso('Matrícula feita.'); await ctx.recarregar(); }
        catch (e) { aviso(e.message, 'erro'); }
      } }, 'Matricular')));
}

// ---------- novo dev ----------

function janelaNovoDev(ctx) {
  const nome = h('input', { type: 'text', maxlength: 50, required: true, placeholder: 'Nome e sobrenome' });
  const email = h('input', { type: 'email', maxlength: 120, required: true, placeholder: 'aluno@exemplo.com', autocomplete: 'off' });
  const senha = h('input', { type: 'text', minlength: 8, maxlength: 72, required: true, autocomplete: 'off', placeholder: 'Mínimo de 8 caracteres' });
  const formulario = h('form', { class: 'formulario', onsubmit: async (e) => {
    e.preventDefault();
    try {
      const novo = await post('/api/devs', { nome: nome.value, email: email.value, senha: senha.value });
      fecharJanela();
      aviso('Dev cadastrado.');
      await ctx.recarregar();
      location.hash = '#/devs/' + novo.id;
    } catch (err) { aviso(err.message, 'erro'); }
  } }, campo('Nome', nome), campo('E-mail de acesso', email), campo('Senha inicial', senha, 'Combine com o aluno; ele entra com esse e-mail e essa senha.'), h('div', { class: 'acoes' },
    h('button', { class: 'botao', type: 'button', onclick: fecharJanela }, 'Cancelar'),
    h('button', { class: 'botao primario', type: 'submit' }, 'Cadastrar')));
  abrirJanela('Novo dev', formulario);
  nome.focus();
}

// ---------- certificado ----------

export function janelaCertificado(c) {
  const folha = h('div', { class: 'certificado', id: 'certificado' },
    h('div', { class: 'certificado-borda' },
      h('img', { src: 'img/icone.svg', alt: '', width: 54, height: 54 }),
      h('p', { class: 'certificado-topo' }, 'Certificado de conclusão'),
      h('p', { class: 'certificado-texto' }, 'Certificamos que'),
      h('h3', { class: 'certificado-nome' }, c.dev),
      h('p', { class: 'certificado-texto' }, 'concluiu a trilha completa do bootcamp'),
      h('p', { class: 'certificado-bootcamp' }, c.bootcamp),
      h('p', { class: 'certificado-texto' }, 'somando ' + numero(c.xp) + ' XP, em ' + dataLonga(c.emitidoEm) + '.'),
      h('p', { class: 'certificado-codigo' }, 'Código ', h('code', {}, c.codigo))));
  abrirJanela('Certificado', [folha, h('div', { class: 'acoes' },
    h('p', { class: 'mudo' }, 'Qualquer pessoa confere o código na tela de entrada da Jornada.'),
    h('button', { class: 'botao primario', type: 'button', onclick: () => window.print() }, icone('imprimir'), 'Imprimir'))], { larga: true });
}
