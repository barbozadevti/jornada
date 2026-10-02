package dev.barboza.jornada.dominio;

public enum SituacaoBootcamp {
    PROXIMO("Começa em breve"),
    EM_ANDAMENTO("Em andamento"),
    ENCERRADO("Encerrado");

    private final String rotulo;

    SituacaoBootcamp(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
