package dev.barboza.jornada.aplicacao;

import static dev.barboza.jornada.dominio.TipoConteudo.CURSO;
import static dev.barboza.jornada.dominio.TipoConteudo.DESAFIO;
import static dev.barboza.jornada.dominio.TipoConteudo.MENTORIA;

import java.time.LocalDate;

import dev.barboza.jornada.aplicacao.Escola.NovoConteudo;
import dev.barboza.jornada.dominio.Bootcamp;

/**
 * Dados de exemplo para a primeira execução: três bootcamps (um em andamento, um que ainda vai começar e
 * um já encerrado) e quatro devs em pontos diferentes da trilha. As datas são relativas a hoje.
 */
final class Exemplos {

    /** Senha de todas as contas de demonstração (dados fictícios, públicos de propósito). */
    static final String SENHA_DEMO = "Jornada@2026";

    private Exemplos() {
    }

    static void preencher(Escola escola) {
        LocalDate hoje = escola.hoje();

        Bootcamp java = escola.criarBootcampEm("Java Developer",
                "Do zero ao primeiro projeto em Java, com orientação a objetos, testes e uma API REST.",
                hoje.minusDays(20), 90, 40, hoje);
        escola.adicionarConteudoEm(java, curso("Java do zero", "Sintaxe, tipos, laços e coleções.", 8));
        escola.adicionarConteudoEm(java, curso("Orientação a objetos", "Abstração, encapsulamento, herança e polimorfismo.", 6));
        escola.adicionarConteudoEm(java, mentoria("Revisão de código com um mentor", "Leitura crítica do seu primeiro projeto.", hoje.minusDays(5)));
        escola.adicionarConteudoEm(java, desafio("Abstraindo um bootcamp", "Modele o próprio bootcamp com POO.", 2));
        escola.adicionarConteudoEm(java, curso("Spring Boot e APIs REST", "Controllers, validação e testes de integração.", 10));
        escola.adicionarConteudoEm(java, mentoria("Portfólio e entrevista técnica", "Como apresentar o que você construiu.", hoje.plusDays(12)));
        escola.adicionarConteudoEm(java, desafio("Projeto final", "Uma API completa, com testes e documentação.", 3));

        Bootcamp dados = escola.criarBootcampEm("Dados com SQL",
                "Modelagem relacional e consultas, do básico ao que cai em entrevista.",
                hoje.plusDays(9), 60, 30, hoje);
        escola.adicionarConteudoEm(dados, curso("Modelagem relacional", "Tabelas, chaves e normalização.", 6));
        escola.adicionarConteudoEm(dados, curso("SQL na prática", "SELECT, JOIN, agrupamentos e subconsultas.", 8));
        escola.adicionarConteudoEm(dados, desafio("Consultas do mundo real", "Responda perguntas de negócio com SQL.", 2));
        escola.adicionarConteudoEm(dados, mentoria("Tire suas dúvidas", "Sessão ao vivo com a turma.", hoje.plusDays(30)));

        Bootcamp web = escola.criarBootcampHistorico("Fundamentos de Web",
                "HTML, CSS e JavaScript para publicar a primeira página.", hoje.minusDays(120), 60, 25);
        escola.adicionarConteudoEm(web, curso("HTML e CSS", "Estrutura e estilo de uma página.", 5));
        escola.adicionarConteudoEm(web, curso("JavaScript essencial", "Variáveis, funções e o DOM.", 8));
        escola.adicionarConteudoEm(web, desafio("Página pessoal", "Publique uma página sobre você.", 1));

        var camila = escola.criarDevEm("Camila Souza");
        var joao = escola.criarDevEm("João Pereira");
        var marina = escola.criarDevEm("Marina Alves");
        var beatriz = escola.criarDevEm("Beatriz Lima");

        // Contas de demonstração: o coordenador vê tudo; cada aluno vê só a própria jornada.
        escola.criarConta("coordenador@jornada.dev", SENHA_DEMO, Papel.COORDENADOR, null, "Roberto Lima");
        escola.criarConta("camila@jornada.dev", SENHA_DEMO, Papel.ALUNO, camila.getId(), camila.getNome());
        escola.criarConta("joao@jornada.dev", SENHA_DEMO, Papel.ALUNO, joao.getId(), joao.getNome());
        escola.criarConta("marina@jornada.dev", SENHA_DEMO, Papel.ALUNO, marina.getId(), marina.getNome());
        escola.criarConta("beatriz@jornada.dev", SENHA_DEMO, Papel.ALUNO, beatriz.getId(), beatriz.getNome());

        // Camila: terminou Fundamentos de Web (tem certificado) e está no meio do Java Developer.
        escola.matricularEm(camila.getId(), web.getId(), hoje.minusDays(110));
        escola.progredirEm(camila.getId(), web.getId(), hoje.minusDays(100));
        escola.progredirEm(camila.getId(), web.getId(), hoje.minusDays(95));
        escola.progredirEm(camila.getId(), web.getId(), hoje.minusDays(90));
        escola.matricularEm(camila.getId(), java.getId(), hoje.minusDays(18));
        escola.progredirEm(camila.getId(), java.getId(), hoje.minusDays(16));
        escola.progredirEm(camila.getId(), java.getId(), hoje.minusDays(12));
        escola.progredirEm(camila.getId(), java.getId(), hoje.minusDays(4));
        escola.progredirEm(camila.getId(), java.getId(), hoje.minusDays(2));

        escola.matricularEm(joao.getId(), java.getId(), hoje.minusDays(17));
        escola.progredirEm(joao.getId(), java.getId(), hoje.minusDays(15));
        escola.progredirEm(joao.getId(), java.getId(), hoje.minusDays(8));

        escola.matricularEm(marina.getId(), java.getId(), hoje.minusDays(10));
        escola.progredirEm(marina.getId(), java.getId(), hoje.minusDays(9));
        escola.matricularEm(marina.getId(), dados.getId(), hoje.minusDays(3));
    }

    private static NovoConteudo curso(String titulo, String descricao, int horas) {
        return new NovoConteudo(CURSO, titulo, descricao, horas, null, null);
    }

    private static NovoConteudo mentoria(String titulo, String descricao, LocalDate data) {
        return new NovoConteudo(MENTORIA, titulo, descricao, null, data, null);
    }

    private static NovoConteudo desafio(String titulo, String descricao, int dificuldade) {
        return new NovoConteudo(DESAFIO, titulo, descricao, null, null, dificuldade);
    }
}
