// Cliente da API: devolve o JSON ou lança um Error com a mensagem pronta para mostrar na tela.
export async function api(metodo, caminho, corpo) {
  const opcoes = { method: metodo, headers: { Accept: 'application/json' } };
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo);
  }
  let resposta;
  try {
    resposta = await fetch(caminho, opcoes);
  } catch {
    throw new Error('Não consegui falar com o servidor. Ele está rodando?');
  }
  if (!resposta.ok) {
    const problema = await resposta.json().catch(() => null);
    throw new Error(problema?.detail || 'Algo deu errado. Tente de novo.');
  }
  return resposta.status === 204 ? null : resposta.json();
}

export const get = (caminho) => api('GET', caminho);
export const post = (caminho, corpo = {}) => api('POST', caminho, corpo);
