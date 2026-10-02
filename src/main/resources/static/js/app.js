import { get, post, quandoPerderSessao } from './api.js';
import { aviso, h } from './ui.js';
import { detalheDoBootcamp, listaDeBootcamps } from './bootcamps.js';
import { listaDeDevs } from './devs.js';
import { telaDeRanking } from './ranking.js';
import { telaPorDentro } from './dentro.js';
import { blocoDoUsuario, telaDeLogin } from './login.js';

const raiz = document.getElementById('principal');
const ctx = {
  dados: { bootcamps: [], devs: [], ranking: [] },
  usuario: null,
  coordenador: false,
  recarregar,
};

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

// ---------- entrada e saída ----------

function mostrarLogin() {
  ctx.usuario = null;
  document.body.classList.add('deslogado');
  document.getElementById('usuario').hidden = true;
  document.getElementById('reiniciar').hidden = true;
  raiz.replaceChildren(telaDeLogin(entrou));
  document.title = 'Jornada · Entrar';
  window.scrollTo({ top: 0 });
}

async function entrou(usuario) {
  ctx.usuario = usuario;
  ctx.coordenador = usuario.papel === 'COORDENADOR';
  try { await get('/api/auth/csrf'); } catch { /* o cookie vem no próximo pedido */ }
  document.body.classList.remove('deslogado');
  const cx = document.getElementById('usuario');
  cx.replaceChildren(...blocoDoUsuario(usuario, sair));
  cx.hidden = false;
  document.getElementById('reiniciar').hidden = !ctx.coordenador;
  // O aluno não tem lista de devs: o menu leva direto para a jornada dele.
  const linkDevs = document.querySelector('#menu a[data-rota="devs"]');
  linkDevs.textContent = ctx.coordenador ? 'Devs' : 'Minha jornada';
  linkDevs.setAttribute('href', ctx.coordenador ? '#/devs' : '#/devs/' + usuario.devId);
  await carregar();
  if (!ctx.coordenador && location.hash === '' ) location.hash = '#/devs/' + usuario.devId;
  await desenhar();
}

async function sair() {
  try { await post('/api/auth/sair'); } catch { /* sai mesmo assim */ }
  location.hash = '';
  mostrarLogin();
}

quandoPerderSessao(() => {
  if (ctx.usuario) {
    aviso('Sua sessão terminou. Entre de novo.', 'erro');
    mostrarLogin();
  }
});

// ---------- telas ----------

async function desenhar(rolar = true) {
  if (!ctx.usuario) return;
  let { secao, id } = rotaAtual();
  if (secao === 'devs' && !ctx.coordenador && id !== ctx.usuario.devId) {
    location.replace('#/devs/' + ctx.usuario.devId);
    return;
  }
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
  const titulo = { devs: ctx.coordenador ? 'Devs' : 'Minha jornada', ranking: 'Ranking', dentro: 'Por dentro' }[secao] || 'Bootcamps';
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

// ---------- partida: já há sessão? ----------

try { await get('/api/auth/csrf'); } catch { /* sem cookie por enquanto */ }
try {
  const usuario = await get('/api/auth/eu');
  await entrou(usuario);
} catch {
  mostrarLogin();
}
