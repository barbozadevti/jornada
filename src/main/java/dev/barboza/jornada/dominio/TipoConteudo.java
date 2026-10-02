package dev.barboza.jornada.dominio;

/** Os tipos de conteúdo que um bootcamp oferece. */
public enum TipoConteudo {
    CURSO("Curso"),
    MENTORIA("Mentoria"),
    DESAFIO("Desafio");

    private final String rotulo;

    TipoConteudo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
