import { get } from './api.js';
import { h, icone, iniciais, numero, dataLonga } from './ui.js';
import { janelaCertificado } from './devs.js';

/** O aluno só abre a própria página; os outros aparecem no ranking, mas sem link. */
const pode = (ctx, d) => ctx.coordenador || d.id === ctx.usuario.devId;

export function telaDeRanking(ctx) {
  const { ranking } = ctx.dados;
  const podio = ranking.slice(0, 3);
  // Ordem visual do pódio: 2º, 1º, 3º.
  const ordem = [podio[1], podio[0], podio[2]].filter(Boolean);

  const codigo = h('input', { type: 'text', placeholder: 'Ex.: JRN-D1-B3', 'aria-label': 'Código do certificado', maxlength: 30 });
  const resultado = h('div', { class: 'resultado-certificado', 'aria-live': 'polite' });
  const conferir = async (e) => {
    e.preventDefault();
    if (!codigo.value.trim()) return;
    try {
      const c = await get('/api/certificados/' + encodeURIComponent(codigo.value.trim()));
      resultado.replaceChildren(h('div', { class: 'valido' }, icone('check', 18),
        h('div', {}, h('strong', {}, 'Certificado autêntico'),
          h('p', {}, c.dev + ' concluiu ' + c.bootcamp + ' em ' + dataLonga(c.emitidoEm) + ' (' + numero(c.xp) + ' XP).'),
          h('button', { class: 'link', type: 'button', onclick: () => janelaCertificado(c) }, 'Abrir certificado'))));
    } catch (err) {
      resultado.replaceChildren(h('div', { class: 'invalido' }, icone('fechar', 18), h('div', {}, h('strong', {}, 'Código não encontrado'), h('p', {}, err.message))));
    }
  };

  return h('section', {},
    h('div', { class: 'cabecalho' }, h('div', {}, h('h1', {}, 'Ranking'), h('p', { class: 'sub' }, 'Quem mais acumulou XP concluindo conteúdos. O desempate é por ordem alfabética.'))),
    ranking.length ? h('div', { class: 'podio' }, ordem.map((d) =>
      h(pode(ctx, d) ? 'a' : 'div', { class: 'podio-lugar lugar-' + d.posicao, href: pode(ctx, d) ? '#/devs/' + d.id : null },
        h('span', { class: 'avatar grande' }, iniciais(d.nome)),
        h('strong', {}, d.nome),
        h('span', { class: 'mudo' }, d.nivel),
        h('span', { class: 'podio-xp' }, numero(d.xp) + ' XP'),
        h('span', { class: 'podio-base' }, d.posicao + 'º')))) : null,
    h('div', { class: 'cartao tabela-cartao' },
      h('table', { class: 'tabela' },
        h('thead', {}, h('tr', {}, h('th', {}, '#'), h('th', {}, 'Dev'), h('th', {}, 'Nível'), h('th', { class: 'num' }, 'Certificados'), h('th', { class: 'num' }, 'XP'))),
        h('tbody', {}, ranking.map((d) => h('tr', {},
          h('td', {}, d.posicao),
          h('td', {}, pode(ctx, d) ? h('a', { href: '#/devs/' + d.id }, d.nome) : d.nome, d.id === ctx.usuario.devId ? h('span', { class: 'chip ouro' }, 'você') : null),
          h('td', {}, d.nivel),
          h('td', { class: 'num' }, d.certificados || '—'),
          h('td', { class: 'num' }, h('strong', {}, numero(d.xp)))))))),
    h('div', { class: 'cartao' },
      h('h3', {}, 'Conferir um certificado'),
      h('p', { class: 'mudo' }, 'Todo certificado traz um código. Digite-o aqui para ver se é autêntico.'),
      h('form', { class: 'linha-campos', onsubmit: conferir }, codigo, h('button', { class: 'botao', type: 'submit' }, 'Conferir')),
      resultado));
}
