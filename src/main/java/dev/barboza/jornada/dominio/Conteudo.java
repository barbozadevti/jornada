package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Abstração: tudo o que um bootcamp oferece (curso, mentoria, desafio) é um conteúdo.
 * Cada tipo sabe calcular o próprio XP e dizer se pode ser concluído hoje; quem usa só enxerga {@code Conteudo}.
 * A hierarquia é fechada ({@code sealed}): os três tipos são conhecidos em tempo de compilação.
 */
public abstract sealed class Conteudo permits Curso, Mentoria, Desafio {

    /** XP-base de qualquer conteúdo concluído. */
    protected static final int XP_PADRAO = 10;

    private final String id;
    private final String titulo;
    private final String descricao;

    protected Conteudo(String id, String titulo, String descricao) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraVioladaException("O conteúdo precisa de um título.");
        }
        if (titulo.trim().length() > 80) {
            throw new RegraVioladaException("O título do conteúdo pode ter até 80 caracteres.");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    /** Polimorfismo: cada subclasse tem a sua regra de XP. */
    public abstract int calcularXp();

    /** De onde vem o XP, em texto, para mostrar ao lado do número. */
    public abstract String explicarXp();

    public abstract TipoConteudo tipo();

    /** O dado que caracteriza o tipo: "8 h", "dia 12/10", "nível 2 de 3". */
    public abstract String detalhe();

    /** Motivo pelo qual o conteúdo ainda não pode ser concluído em {@code hoje}; vazio se pode. */
    public Optional<String> impedimentoParaConcluir(LocalDate hoje) {
        return Optional.empty();
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return tipo().rotulo() + " \"" + titulo + "\" (" + calcularXp() + " XP)";
    }
}
