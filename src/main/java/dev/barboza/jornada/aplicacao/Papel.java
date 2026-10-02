package dev.barboza.jornada.aplicacao;

/** Quem a pessoa é na plataforma: o coordenador cuida de tudo; o aluno só da própria jornada. */
public enum Papel {
    COORDENADOR("Coordenador"),
    ALUNO("Aluno");

    private final String rotulo;

    Papel(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
