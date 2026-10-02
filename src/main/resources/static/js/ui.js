// Pequenos utilitários de interface. Tudo é montado com o DOM (textContent), nunca com innerHTML,
// então nenhum dado vindo da API consegue injetar HTML.

const SVG = 'http://www.w3.org/2000/svg';

/** h('div', { class: 'x', onclick: fn, 'data-id': 1 }, 'texto', outroNo) */
export function h(tag, props = {}, ...filhos) {
  const no = document.createElement(tag);
  for (const [chave, valor] of Object.entries(props || {})) {
    if (valor === undefined || valor === null || valor === false) continue;
    if (chave.startsWith('on')) no.addEventListener(chave.slice(2), valor);
    else if (chave === 'class') no.className = valor;
    else if (chave === 'texto') no.textContent = valor;
    else if (valor === true) no.setAttribute(chave, '');
    else no.setAttribute(chave, valor);
  }
  anexar(no, filhos);
  return no;
}

function anexar(no, filhos) {
  for (const filho of filhos.flat(Infinity)) {
    if (filho === undefined || filho === null || filho === false) continue;
    no.append(filho.nodeType ? filho : document.createTextNode(String(filho)));
  }
}

/** Ícones simples de traço, desenhados com caminhos SVG. */
const ICONES = {
  curso: 'M4 5.5A1.5 1.5 0 0 1 5.5 4H11v15H5.5A1.5 1.5 0 0 0 4 20.5zM20 5.5A1.5 1.5 0 0 0 18.5 4H13v15h5.5a1.5 1.5 0 0 1 1.5 1.5z',
  mentoria: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 20c0-3.5 3.6-6 8-6s8 2.5 8 6',
  desafio: 'M8 4v16M8 5h10l-2.5 4L18 13H8',
  check: 'M5 12.5l4.5 4.5L19 7.5',
  cadeado: 'M7 11V8a5 5 0 0 1 10 0v3M6 11h12v9H6z',
  relogio: 'M12 7v5l3 2M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z',
  vagas: 'M16 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM8 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM2 19c0-2.6 2.7-4.5 6-4.5M22 19c0-2.6-2.7-4.5-6-4.5M8 19c0-2.6 2.2-4.5 4-4.5s4 1.9 4 4.5',
  estrela: 'M12 3l2.7 5.6 6.1.8-4.5 4.2 1.1 6-5.4-3-5.4 3 1.1-6L3.2 9.4l6.1-.8z',
  mais: 'M12 5v14M5 12h14',
  voltar: 'M15 5l-7 7 7 7',
  imprimir: 'M7 9V4h10v5M7 17H5a1 1 0 0 1-1-1v-5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v5a1 1 0 0 1-1 1h-2M7 14h10v6H7z',
  fechar: 'M6 6l12 12M18 6L6 18',
  trofeu: 'M8 4h8v5a4 4 0 0 1-8 0zM8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4M12 13v4M8.5 20h7M10 17h4',
};

export function icone(nome, tamanho = 18) {
  const svg = document.createElementNS(SVG, 'svg');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('width', tamanho);
  svg.setAttribute('height', tamanho);
  svg.setAttribute('fill', 'none');
  svg.setAttribute('stroke', 'currentColor');
  svg.setAttribute('stroke-width', '1.8');
  svg.setAttribute('stroke-linecap', 'round');
  svg.setAttribute('stroke-linejoin', 'round');
  svg.setAttribute('aria-hidden', 'true');
  const caminho = document.createElementNS(SVG, 'path');
  caminho.setAttribute('d', ICONES[nome] || '');
  svg.append(caminho);
  return svg;
}

export const dataCurta = (iso) => new Date(iso + 'T12:00:00').toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
export const dataLonga = (iso) => new Date(iso + 'T12:00:00').toLocaleDateString('pt-BR');
export const numero = (n) => n.toLocaleString('pt-BR');

export const iniciais = (nome) => nome.split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');

/** Barra de progresso: a largura vai pelo CSSOM, que a política de segurança permite. */
export function barra(percentual, rotulo, classe = '') {
  const dentro = h('span', { class: 'barra-dentro' });
  dentro.style.width = Math.max(0, Math.min(100, percentual)) + '%';
  return h('div', { class: 'barra ' + classe, role: 'progressbar', 'aria-valuemin': 0, 'aria-valuemax': 100,
    'aria-valuenow': percentual, 'aria-label': rotulo }, dentro);
}

export function chip(texto, classe = '') {
  return h('span', { class: 'chip ' + classe }, texto);
}

export function chipSituacao(situacao, rotulo) {
  return chip(rotulo, 'sit-' + situacao.toLowerCase());
}

export function chipTipo(tipo, rotulo) {
  return h('span', { class: 'chip tipo tipo-' + tipo.toLowerCase() }, icone(tipo.toLowerCase(), 14), rotulo);
}

// ---------- avisos e janela ----------

export function aviso(texto, tipo = 'ok') {
  const caixa = document.getElementById('avisos');
  const no = h('div', { class: 'aviso aviso-' + tipo }, texto);
  caixa.append(no);
  setTimeout(() => no.classList.add('saindo'), tipo === 'erro' ? 5200 : 3200);
  setTimeout(() => no.remove(), tipo === 'erro' ? 5600 : 3600);
}

export function abrirJanela(titulo, corpo, { larga = false } = {}) {
  const janela = document.getElementById('janela');
  janela.className = larga ? 'larga' : '';
  const fechar = h('button', { class: 'icone-botao', type: 'button', 'aria-label': 'Fechar', onclick: () => janela.close() }, icone('fechar'));
  janela.replaceChildren(
    h('div', { class: 'janela-topo' }, h('h2', { id: 'janela-titulo' }, titulo), fechar),
    h('div', { class: 'janela-corpo' }, corpo));
  if (!janela.open) janela.showModal();
  return janela;
}

export function fecharJanela() {
  const janela = document.getElementById('janela');
  if (janela.open) janela.close();
}

/** Campo de formulário com rótulo. */
export function campo(rotulo, entrada, dica) {
  return h('label', { class: 'campo' }, h('span', { class: 'campo-rotulo' }, rotulo), entrada,
    dica ? h('span', { class: 'campo-dica' }, dica) : null);
}
