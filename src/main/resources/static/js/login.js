import { get, post } from './api.js';
import { campo, dataLonga, h, icone, iniciais, numero } from './ui.js';

const SENHA_DEMO = 'Jornada@2026';

const CONTAS_DEMO = [
  { email: 'coordenador@jornada.dev', nome: 'Roberto Lima', papel: 'Coordenador', dica: 'cria bootcamps, monta trilhas e cadastra alunos' },
  { email: 'camila@jornada.dev', nome: 'Camila Souza', papel: 'Aluna', dica: 'já tem um certificado e está no meio do Java Developer' },
  { email: 'joao@jornada.dev', nome: 'João Pereira', papel: 'Aluno', dica: 'no começo da trilha' },
  { email: 'marina@jornada.dev', nome: 'Marina Alves', papel: 'Aluna', dica: 'matriculada em dois bootcamps' },
  { email: 'beatriz@jornada.dev', nome: 'Beatriz Lima', papel: 'Aluna', dica: 'ainda sem matrícula' },
];

/** Tela de entrada. Chama aoEntrar(usuario) quando o login dá certo. */
export function telaDeLogin(aoEntrar) {
  const email = h('input', { type: 'email', id: 'login-email', autocomplete: 'username', required: true, maxlength: 120, placeholder: 'voce@exemplo.com' });
  const senha = h('input', { type: 'password', id: 'login-senha', autocomplete: 'current-password', required: true, maxlength: 72 });
  const erro = h('p', { class: 'erro-login', role: 'alert' });
  const botao = h('button', { class: 'botao primario grande', type: 'submit' }, 'Entrar');

  async function entrar(e) {
    if (e) e.preventDefault();
    erro.textContent = '';
    botao.disabled = true;
    try {
      const usuario = await post('/api/auth/entrar', { email: email.value, senha: senha.value });
      aoEntrar(usuario);
    } catch (err) {
      erro.textContent = err.message;
      senha.value = '';
      senha.focus();
    } finally {
      botao.disabled = false;
    }
  }

  const demo = h('div', { class: 'demo-contas' },
    h('h2', {}, 'Contas de demonstração'),
    h('p', { class: 'mudo' }, 'Dados fictícios. A senha de todas é ', h('code', {}, SENHA_DEMO), '. Clique para entrar.'),
    h('div', { class: 'demo-lista' }, CONTAS_DEMO.map((c) =>
      h('button', { class: 'demo-conta', type: 'button', onclick: () => { email.value = c.email; senha.value = SENHA_DEMO; entrar(); } },
        h('span', { class: 'avatar' }, iniciais(c.nome)),
        h('span', { class: 'dev-texto' }, h('strong', {}, c.nome), h('span', { class: 'mudo' }, c.papel + ' · ' + c.dica))))));

  const codigo = h('input', { type: 'text', id: 'cert-codigo', maxlength: 30, placeholder: 'Ex.: JRN-D1-B3', 'aria-label': 'Código do certificado' });
  const resultado = h('div', { 'aria-live': 'polite' });
  const conferir = async (e) => {
    e.preventDefault();
    if (!codigo.value.trim()) return;
    try {
      const c = await get('/api/certificados/' + encodeURIComponent(codigo.value.trim()));
      resultado.replaceChildren(h('div', { class: 'valido' }, icone('check', 18), h('div', {}, h('strong', {}, 'Certificado autêntico'),
        h('p', {}, c.dev + ' concluiu ' + c.bootcamp + ' em ' + dataLonga(c.emitidoEm) + ' (' + numero(c.xp) + ' XP).'))));
    } catch (err) {
      resultado.replaceChildren(h('div', { class: 'invalido' }, icone('fechar', 18), h('div', {}, h('strong', {}, 'Código não encontrado'), h('p', {}, err.message))));
    }
  };
  const verificador = h('div', { class: 'cartao' },
    h('h2', {}, 'Conferir um certificado'),
    h('p', { class: 'mudo' }, 'Recebeu um certificado da Jornada? Confira o código, sem precisar de login.'),
    h('form', { class: 'linha-campos', onsubmit: conferir }, codigo, h('button', { class: 'botao', type: 'submit' }, 'Conferir')),
    resultado);

  return h('section', { class: 'login' },
    h('div', { class: 'cartao login-cartao' },
      h('img', { src: 'img/icone.svg', alt: '', width: 56, height: 56 }),
      h('h1', {}, 'Entrar na Jornada'),
      h('p', { class: 'sub' }, 'O coordenador cuida dos bootcamps. Cada aluno vê só a própria jornada.'),
      h('form', { class: 'formulario', onsubmit: entrar },
        campo('E-mail', email), campo('Senha', senha), erro, botao)),
    demo,
    verificador);
}

export function blocoDoUsuario(usuario, aoSair) {
  return [
    h('span', { class: 'avatar pequeno' }, iniciais(usuario.nome)),
    h('span', { class: 'usuario-nome' }, usuario.nome, h('small', {}, usuario.papelRotulo)),
    h('button', { class: 'link claro', type: 'button', onclick: aoSair }, icone('voltar', 14), 'Sair'),
  ];
}
