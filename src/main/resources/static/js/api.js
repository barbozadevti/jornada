let aoPerderSessao = () => {};

/** Chamado quando a API responde 401 fora do login: a sessão acabou e o site volta para a tela de entrada. */
export function quandoPerderSessao(funcao) {
  aoPerderSessao = funcao;
}

function lerCookie(nome) {
  const par = document.cookie.split('; ').find((c) => c.startsWith(nome + '='));
  return par ? decodeURIComponent(par.slice(nome.length + 1)) : null;
}

// Cliente da API: devolve o JSON ou lança um Error com a mensagem pronta para mostrar na tela.
export async function api(metodo, caminho, corpo) {
  const opcoes = { method: metodo, headers: { Accept: 'application/json' } };
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo);
  }
  if (metodo !== 'GET') {
    const token = lerCookie('XSRF-TOKEN');
    if (token) opcoes.headers['X-XSRF-TOKEN'] = token;
  }
  let resposta;
  try {
    resposta = await fetch(caminho, opcoes);
  } catch {
    throw new Error('Não consegui falar com o servidor. Ele está rodando?');
  }
  if (!resposta.ok) {
    const problema = await resposta.json().catch(() => null);
    if (resposta.status === 401 && !caminho.startsWith('/api/auth/')) aoPerderSessao();
    throw new Error(problema?.detail || 'Algo deu errado. Tente de novo.');
  }
  return resposta.status === 204 ? null : resposta.json();
}

export const get = (caminho) => api('GET', caminho);
export const post = (caminho, corpo = {}) => api('POST', caminho, corpo);
