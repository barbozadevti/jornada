import { post } from './api.js';
import { abrirJanela, aviso, barra, campo, chip, chipSituacao, chipTipo, dataCurta, dataLonga, fecharJanela, h, icone, iniciais, numero } from './ui.js';

// ---------- lista ----------

export function listaDeBootcamps(ctx) {
  const { bootcamps } = ctx.dados;
  return h('section', {},
    h('div', { class: 'cabecalho' },
      h('div', {},
        h('h1', {}, 'Bootcamps'),
        h('p', { class: 'sub' }, 'Cada bootcamp é uma trilha de cursos, mentorias e desafios. Concluir um conteúdo rende XP.')),
      ctx.coordenador ? h('button', { class: 'botao primario', type: 'button', onclick: () => janelaNovoBootcamp(ctx) }, icone('mais'), 'Novo bootcamp') : null),
    h('div', { class: 'grade' }, bootcamps.map((b) => cartao(b))));
}

function cartao(b) {
  const lotacao = Math.round((b.matriculados / b.vagas) * 100);
  const tipos = ['CURSO', 'MENTORIA', 'DESAFIO'].map((t) => {
    const n = b.conteudos.filter((c) => c.tipo === t).length;
    return n ? h('span', { class: 'mini tipo-' + t.toLowerCase(), title: n + ' ' + t.toLowerCase() + (n > 1 ? 's' : '') }, icone(t.toLowerCase(), 14), n) : null;
  });
  return h('a', { class: 'cartao bootcamp-cartao', href: '#/bootcamps/' + b.id },
    h('div', { class: 'linha' }, chipSituacao(b.situacao, b.situacaoRotulo), h('span', { class: 'xp-total' }, icone('estrela', 14), numero(b.xpTotal) + ' XP')),
    h('h2', {}, b.nome),
    h('p', { class: 'descricao' }, b.descricao),
    h('div', { class: 'meta' }, icone('relogio', 14), dataCurta(b.dataInicial) + ' a ' + dataLonga(b.dataFinal)),
    h('div', { class: 'minis' }, tipos, h('span', { class: 'mini mudo' }, b.conteudos.length + ' conteúdos')),
    h('div', { class: 'vagas' },
      h('div', { class: 'linha mudo' }, h('span', {}, icone('vagas', 14), ' ' + b.matriculados + ' de ' + b.vagas + ' vagas'), h('span', {}, lotacao + '%')),
      barra(lotacao, 'Vagas ocupadas', 'fina')));
}

// ---------- detalhe ----------

export function detalheDoBootcamp(ctx, id) {
  const b = ctx.dados.bootcamps.find((x) => x.id === id);
  if (!b) return h('p', { class: 'vazio' }, 'Esse bootcamp não existe.');
  const matriculadosIds = new Set(ctx.dados.devs.filter((d) => d.matriculas.some((m) => m.bootcampId === b.id)).map((d) => d.id));

  const trilha = h('ol', { class: 'trilha' }, b.conteudos.map((c, i) =>
    h('li', { class: 'passo tipo-' + c.tipo.toLowerCase() },
      h('span', { class: 'passo-numero' }, i + 1),
      h('div', { class: 'passo-corpo' },
        h('div', { class: 'linha' }, h('strong', {}, c.titulo), h('span', { class: 'xp', title: c.explicacaoXp }, '+' + c.xp + ' XP')),
        h('div', { class: 'passo-meta' }, chipTipo(c.tipo, c.tipoRotulo), h('span', { class: 'mudo' }, c.detalhe)),
        c.descricao ? h('p', { class: 'mudo' }, c.descricao) : null,
        h('p', { class: 'conta' }, h('code', {}, c.explicacaoXp))))));

  return h('section', {},
    h('a', { class: 'voltar', href: '#/bootcamps' }, icone('voltar', 16), 'Todos os bootcamps'),
    h('div', { class: 'cabecalho' },
      h('div', {},
        h('div', { class: 'linha-chips' }, chipSituacao(b.situacao, b.situacaoRotulo), chip(dataLonga(b.dataInicial) + ' a ' + dataLonga(b.dataFinal))),
        h('h1', {}, b.nome),
        h('p', { class: 'sub' }, b.descricao)),
      h('div', { class: 'numeros' },
        numeroGrande(numero(b.xpTotal), 'XP no total'),
        numeroGrande(b.conteudos.length, 'conteúdos'),
        numeroGrande(b.vagasRestantes, 'vagas livres'))),
    h('div', { class: 'duas-colunas' },
      h('div', {},
        h('h2', { class: 'secao' }, 'Trilha'),
        b.conteudos.length ? trilha : h('p', { class: 'vazio' }, 'Ainda não há conteúdos. Adicione o primeiro ao lado.')),
      h('div', { class: 'lateral' },
        painelMatricula(ctx, b, matriculadosIds),
        !ctx.coordenador ? null
          : b.trilhaCongelada
            ? h('div', { class: 'cartao nota' }, icone('cadeado', 18), h('p', {}, 'A trilha está congelada: já há alunos matriculados, então não dá para acrescentar conteúdos.'))
            : painelNovoConteudo(ctx, b))));
}

function numeroGrande(valor, rotulo) {
  return h('div', { class: 'numero' }, h('strong', {}, valor), h('span', {}, rotulo));
}

function painelMatricula(ctx, b, matriculadosIds) {
  if (!ctx.coordenador) return painelMatriculaDoAluno(ctx, b, matriculadosIds);
  const disponiveis = ctx.dados.devs.filter((d) => !matriculadosIds.has(d.id));
  const matriculados = ctx.dados.devs.filter((d) => matriculadosIds.has(d.id));
  const seletor = h('select', { id: 'dev-matricula' }, disponiveis.map((d) => h('option', { value: d.id }, d.nome)));
  const botao = h('button', { class: 'botao primario', type: 'button', disabled: !disponiveis.length || b.situacao === 'ENCERRADO' || !b.conteudos.length || b.vagasRestantes === 0,
    onclick: async () => {
      try {
        await post('/api/devs/' + seletor.value + '/matriculas', { bootcampId: b.id });
        aviso('Matrícula feita.');
        await ctx.recarregar();
      } catch (e) { aviso(e.message, 'erro'); }
    } }, 'Matricular');
  return h('div', { class: 'cartao' },
    h('h3', {}, 'Matrículas'),
    disponiveis.length
      ? h('div', { class: 'linha-campos' }, campo('Dev', seletor), botao)
      : h('p', { class: 'mudo' }, 'Todos os devs já estão neste bootcamp.'),
    b.situacao === 'ENCERRADO' ? h('p', { class: 'mudo' }, 'Bootcamp encerrado: não aceita novas matrículas.') : null,
    matriculados.length ? h('ul', { class: 'lista-pessoas' }, matriculados.map((d) => {
      const m = d.matriculas.find((x) => x.bootcampId === b.id);
      return h('li', {}, h('a', { href: '#/devs/' + d.id },
        h('span', { class: 'avatar pequeno' }, iniciais(d.nome)),
        h('span', { class: 'nome' }, d.nome),
        m.certificado ? h('span', { class: 'mini ouro', title: 'Certificado emitido' }, icone('trofeu', 14)) : null,
        h('span', { class: 'mudo' }, m.percentual + '%')));
    })) : null);
}

/** O aluno só se matricula a si mesmo. */
function painelMatriculaDoAluno(ctx, b, matriculadosIds) {
  const jaEsta = matriculadosIds.has(ctx.usuario.devId);
  const bloqueio = b.situacao === 'ENCERRADO' ? 'Bootcamp encerrado: não aceita novas matrículas.'
    : !b.conteudos.length ? 'A trilha ainda não tem conteúdos.'
    : b.vagasRestantes === 0 ? 'Não há mais vagas.' : null;
  const botao = h('button', { class: 'botao primario grande', type: 'button', disabled: !!bloqueio,
    onclick: async () => {
      try {
        await post('/api/devs/' + ctx.usuario.devId + '/matriculas', { bootcampId: b.id });
        aviso('Matrícula feita. Bom estudo!');
        await ctx.recarregar();
      } catch (e) { aviso(e.message, 'erro'); }
    } }, 'Quero me matricular');
  return h('div', { class: 'cartao' },
    h('h3', {}, 'Sua matrícula'),
    jaEsta
      ? h('div', { class: 'concluida' }, h('p', {}, icone('check', 18), ' Você está neste bootcamp.'), h('a', { class: 'botao', href: '#/devs/' + ctx.usuario.devId }, 'Ver minha jornada'))
      : [botao, bloqueio ? h('p', { class: 'mudo' }, bloqueio) : null]);
}

function painelNovoConteudo(ctx, b) {
  const tipo = h('select', { id: 'tipo-conteudo' },
    h('option', { value: 'CURSO' }, 'Curso'), h('option', { value: 'MENTORIA' }, 'Mentoria'), h('option', { value: 'DESAFIO' }, 'Desafio'));
  const titulo = h('input', { type: 'text', maxlength: 80, required: true, placeholder: 'Ex.: Testes com JUnit' });
  const descricao = h('input', { type: 'text', maxlength: 300, placeholder: 'Opcional' });
  const horas = h('input', { type: 'number', min: 1, max: 200, value: 4 });
  const data = h('input', { type: 'date' });
  const nivel = h('select', {}, h('option', { value: 1 }, '1 · fácil'), h('option', { value: 2, selected: true }, '2 · médio'), h('option', { value: 3 }, '3 · difícil'));
  const camposTipo = h('div', {});
  const regra = h('p', { class: 'conta' });

  const atualizar = () => {
    const t = tipo.value;
    if (t === 'CURSO') { camposTipo.replaceChildren(campo('Carga horária (h)', horas)); regra.textContent = 'Curso: 10 XP por hora.'; }
    if (t === 'MENTORIA') { camposTipo.replaceChildren(campo('Dia da mentoria', data)); regra.textContent = 'Mentoria: 10 + 20 XP. Só pode ser concluída a partir do dia marcado.'; }
    if (t === 'DESAFIO') { camposTipo.replaceChildren(campo('Dificuldade', nivel)); regra.textContent = 'Desafio: 10 XP + 15 por nível de dificuldade.'; }
  };
  tipo.addEventListener('change', atualizar);
  atualizar();

  const formulario = h('form', { class: 'formulario', onsubmit: async (e) => {
    e.preventDefault();
    const corpo = { tipo: tipo.value, titulo: titulo.value, descricao: descricao.value };
    if (tipo.value === 'CURSO') corpo.cargaHoraria = Number(horas.value);
    if (tipo.value === 'MENTORIA') corpo.data = data.value || null;
    if (tipo.value === 'DESAFIO') corpo.dificuldade = Number(nivel.value);
    try {
      await post('/api/bootcamps/' + b.id + '/conteudos', corpo);
      aviso('Conteúdo acrescentado à trilha.');
      await ctx.recarregar();
    } catch (err) { aviso(err.message, 'erro'); }
  } },
    campo('Tipo', tipo), campo('Título', titulo), campo('Descrição', descricao), camposTipo, regra,
    h('button', { class: 'botao primario', type: 'submit' }, icone('mais'), 'Adicionar à trilha'));
  return h('div', { class: 'cartao' }, h('h3', {}, 'Novo conteúdo'), formulario);
}

// ---------- novo bootcamp ----------

export function janelaNovoBootcamp(ctx) {
  const nome = h('input', { type: 'text', maxlength: 60, required: true, placeholder: 'Ex.: Testes automatizados' });
  const descricao = h('textarea', { maxlength: 300, rows: 3, placeholder: 'Para quem é e o que se aprende' });
  const inicio = h('input', { type: 'date', required: true, value: new Date().toLocaleDateString('sv-SE') });
  const dias = h('input', { type: 'number', min: 1, max: 365, value: 60, required: true });
  const vagas = h('input', { type: 'number', min: 1, max: 500, value: 30, required: true });
  const formulario = h('form', { class: 'formulario', onsubmit: async (e) => {
    e.preventDefault();
    try {
      const criado = await post('/api/bootcamps', { nome: nome.value, descricao: descricao.value, dataInicial: inicio.value,
        duracaoEmDias: Number(dias.value), vagas: Number(vagas.value) });
      fecharJanela();
      aviso('Bootcamp criado. Agora monte a trilha.');
      await ctx.recarregar();
      location.hash = '#/bootcamps/' + criado.id;
    } catch (err) { aviso(err.message, 'erro'); }
  } },
    campo('Nome', nome), campo('Descrição', descricao),
    h('div', { class: 'tres-campos' }, campo('Começa em', inicio), campo('Duração (dias)', dias), campo('Vagas', vagas)),
    h('div', { class: 'acoes' },
      h('button', { class: 'botao', type: 'button', onclick: fecharJanela }, 'Cancelar'),
      h('button', { class: 'botao primario', type: 'submit' }, 'Criar bootcamp')));
  abrirJanela('Novo bootcamp', formulario);
  nome.focus();
}
