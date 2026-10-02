import { get, post } from './api.js';
import { aviso, h } from './ui.js';
import { detalheDoBootcamp, listaDeBootcamps } from './bootcamps.js';
import { listaDeDevs } from './devs.js';
import { telaDeRanking } from './ranking.js';
import { telaPorDentro } from './dentro.js';

const raiz = document.getElementById('principal');
const ctx = { dados: { bootcamps: [], devs: [], ranking: [] }, recarregar, navegar: () => desenhar() };

async function carregar() {
  const [bootcamps, devs, ranking] = await Promise.all([get('/api/bootcamps'), get('/api/devs'), get('/api/ranking')]);
  ctx.dados = { bootcamps, devs, ranking };
}

async function recarregar() {
  await carregar();
  await desenhar(false);
}

function rotaAtual() {
  const [, secao = 'bootcamps', id] = location.hash.split('/');
  return { secao, id };
}

async function desenhar(rolar = true) {
  const { secao, id } = rotaAtual();
  document.querySelectorAll('#menu a').forEach((a) => {
    const ativo = a.dataset.rota === secao;
    a.classList.toggle('ativo', ativo);
    if (ativo) a.setAttribute('aria-current', 'page'); else a.removeAttribute('aria-current');
  });
  let tela;
  try {
    if (secao === 'devs') tela = listaDeDevs(ctx, id);
    else if (secao === 'ranking') tela = telaDeRanking(ctx);
    else if (secao === 'dentro') tela = await telaPorDentro();
    else if (id) tela = detalheDoBootcamp(ctx, id);
    else tela = listaDeBootcamps(ctx);
  } catch (e) {
    tela = h('p', { class: 'vazio' }, e.message);
  }
  raiz.replaceChildren(tela);
  const titulo = { devs: 'Devs', ranking: 'Ranking', dentro: 'Por dentro' }[secao] || 'Bootcamps';
  document.title = 'Jornada · ' + titulo;
  if (rolar) window.scrollTo({ top: 0 });
}

window.addEventListener('hashchange', () => desenhar());

document.getElementById('reiniciar').addEventListener('click', async () => {
  if (!confirm('Apagar tudo o que foi cadastrado e voltar aos dados de exemplo?')) return;
  try {
    await post('/api/demo/reiniciar');
    location.hash = '#/bootcamps';
    await recarregar();
    aviso('Dados de exemplo restaurados.');
  } catch (e) { aviso(e.message, 'erro'); }
});

try {
  await carregar();
} catch (e) {
  aviso(e.message, 'erro');
}
await desenhar();
