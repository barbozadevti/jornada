import { get } from './api.js';
import { chipTipo, h, icone } from './ui.js';

const PILARES = [
  {
    nome: 'Abstração',
    resumo: 'Curso, mentoria e desafio são coisas diferentes, mas para a trilha todos são um Conteudo: algo com título que sabe dizer quanto XP vale.',
    arquivo: 'Conteudo.java',
    codigo: `public abstract sealed class Conteudo
        permits Curso, Mentoria, Desafio {

    public abstract int calcularXp();
    public abstract String explicarXp();
}`,
  },
  {
    nome: 'Herança',
    resumo: 'Cada tipo herda o que é comum e acrescenta só o que é seu: carga horária, data ou dificuldade. A hierarquia é fechada (sealed).',
    arquivo: 'Curso.java · Mentoria.java · Desafio.java',
    codigo: `public final class Curso extends Conteudo {
    private final int cargaHoraria;
    ...
}`,
  },
  {
    nome: 'Polimorfismo',
    resumo: 'Quem soma o XP não pergunta de que tipo é cada conteúdo. Cada um responde pelo seu jeito, e a mentoria ainda decide se já pode ser concluída.',
    arquivo: 'Matricula.java · Dev.java',
    codigo: `int xp = concluidos.keySet().stream()
        .mapToInt(Conteudo::calcularXp)
        .sum();

proximo.impedimentoParaConcluir(hoje);`,
  },
  {
    nome: 'Encapsulamento',
    resumo: 'Os campos são privados e imutáveis, as listas só saem como visões somente-leitura e os construtores recusam dados inválidos. Depois da primeira matrícula a trilha congela.',
    arquivo: 'Bootcamp.java',
    codigo: `public List<Conteudo> getConteudos() {
    return Collections.unmodifiableList(conteudos);
}

if (!matriculas.isEmpty()) {
    throw new EstadoInvalidoException(...);
}`,
  },
];

export async function telaPorDentro() {
  const regras = await get('/api/regras-xp');
  return h('section', {},
    h('div', { class: 'cabecalho' }, h('div', {},
      h('h1', {}, 'Por dentro'),
      h('p', { class: 'sub' }, 'A Jornada nasceu de um desafio de orientação a objetos: abstrair um bootcamp. Aqui está o que o domínio em Java mostra, com o código por trás de cada tela.'))),
    h('div', { class: 'grade pilares' }, PILARES.map((p) =>
      h('article', { class: 'cartao pilar' },
        h('h2', {}, p.nome),
        h('p', {}, p.resumo),
        h('pre', {}, h('code', {}, p.codigo)),
        h('p', { class: 'arquivo mudo' }, p.arquivo)))),
    h('h2', { class: 'secao' }, 'Como cada tipo calcula o XP'),
    h('div', { class: 'cartao tabela-cartao' }, h('table', { class: 'tabela' },
      h('thead', {}, h('tr', {}, h('th', {}, 'Tipo'), h('th', {}, 'Regra'), h('th', {}, 'Exemplo'))),
      h('tbody', {}, regras.map((r) => h('tr', {}, h('td', {}, chipTipo(r.tipo, r.tipoRotulo)), h('td', {}, r.regra), h('td', {}, h('code', {}, r.exemplo))))))),
    h('h2', { class: 'secao' }, 'Regras do bootcamp'),
    h('ul', { class: 'regras' },
      ['A trilha é uma sequência: o próximo conteúdo só libera depois de concluir o anterior.',
        'A mentoria só pode ser concluída a partir do dia marcado.',
        'Não se conclui nada antes de o bootcamp começar nem depois de ele terminar.',
        'A matrícula é recusada se o bootcamp acabou, está sem vagas, sem conteúdos ou se o dev já está nele.',
        'Concluir o último conteúdo emite um certificado com código verificável.',
        'O nível do dev vem só do XP: Iniciante, Júnior (150), Pleno (400) e Sênior (800).'].map((t) => h('li', {}, icone('check', 16), t))),
    h('h2', { class: 'secao' }, 'Diagrama de classes'),
    h('figure', { class: 'cartao diagrama' },
      h('img', { src: 'img/uml.svg', alt: 'Diagrama de classes UML do domínio: Conteudo e suas subclasses Curso, Mentoria e Desafio; Bootcamp, Matricula, Dev, Certificado e os enums.' }),
      h('figcaption', { class: 'mudo' }, 'Um teste lê docs/uml/jornada.mmd e confere cada classe, método e relação contra o código: se o diagrama mentir, o build quebra.')),
    h('p', { class: 'mudo' }, h('a', { href: 'swagger-ui.html', target: '_blank', rel: 'noopener' }, 'Documentação da API (Swagger)')));
}
