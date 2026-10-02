package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulamento: o bootcamp guarda a trilha de conteúdos e as matrículas em listas privadas e só
 * entrega visões somente-leitura. Depois da primeira matrícula a trilha fica congelada, para ninguém
 * perder o lugar no meio do caminho.
 */
public class Bootcamp {

    private final String id;
    private final String nome;
    private final String descricao;
    private final LocalDate dataInicial;
    private final int duracaoEmDias;
    private final int vagas;
    private final List<Conteudo> conteudos = new ArrayList<>();
    private final List<Matricula> matriculas = new ArrayList<>();

    public Bootcamp(String id, String nome, String descricao, LocalDate dataInicial, int duracaoEmDias, int vagas) {
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("O bootcamp precisa de um nome.");
        }
        if (nome.trim().length() > 60) {
            throw new RegraVioladaException("O nome do bootcamp pode ter até 60 caracteres.");
        }
        if (dataInicial == null) {
            throw new RegraVioladaException("Informe a data de início do bootcamp.");
        }
        if (duracaoEmDias < 1 || duracaoEmDias > 365) {
            throw new RegraVioladaException("A duração do bootcamp vai de 1 a 365 dias.");
        }
        if (vagas < 1 || vagas > 500) {
            throw new RegraVioladaException("O bootcamp precisa de 1 a 500 vagas.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.dataInicial = dataInicial;
        this.duracaoEmDias = duracaoEmDias;
        this.vagas = vagas;
    }

    public Bootcamp adicionar(Conteudo conteudo) {
        if (!matriculas.isEmpty()) {
            throw new EstadoInvalidoException("A trilha não pode mudar depois que há alunos matriculados.");
        }
        boolean repetido = conteudos.stream().anyMatch(c -> c.getTitulo().equalsIgnoreCase(conteudo.getTitulo()));
        if (repetido) {
            throw new EstadoInvalidoException("O conteúdo \"" + conteudo.getTitulo() + "\" já está na trilha.");
        }
        conteudos.add(conteudo);
        return this;
    }

    /** Chamado por {@link Dev#matricular(Bootcamp, LocalDate)}, que é quem valida as regras de matrícula. */
    void registrar(Matricula matricula) {
        matriculas.add(matricula);
    }

    public SituacaoBootcamp situacao(LocalDate hoje) {
        if (hoje.isBefore(dataInicial)) {
            return SituacaoBootcamp.PROXIMO;
        }
        return hoje.isAfter(getDataFinal()) ? SituacaoBootcamp.ENCERRADO : SituacaoBootcamp.EM_ANDAMENTO;
    }

    public boolean temVaga() {
        return matriculas.size() < vagas;
    }

    public int calcularXpTotal() {
        return conteudos.stream().mapToInt(Conteudo::calcularXp).sum();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataInicial.plusDays(duracaoEmDias);
    }

    public int getDuracaoEmDias() {
        return duracaoEmDias;
    }

    public int getVagas() {
        return vagas;
    }

    public List<Conteudo> getConteudos() {
        return Collections.unmodifiableList(conteudos);
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }
}
