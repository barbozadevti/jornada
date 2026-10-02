package dev.barboza.jornada.dominio;

/** Herança: um projeto prático; a dificuldade (1 a 3) multiplica o bônus de XP. */
public final class Desafio extends Conteudo {

    private static final int XP_POR_NIVEL = 15;
    private static final int DIFICULDADE_MAXIMA = 3;

    private final int dificuldade;

    public Desafio(String id, String titulo, String descricao, int dificuldade) {
        super(id, titulo, descricao);
        if (dificuldade < 1 || dificuldade > DIFICULDADE_MAXIMA) {
            throw new RegraVioladaException("A dificuldade do desafio vai de 1 a " + DIFICULDADE_MAXIMA + ".");
        }
        this.dificuldade = dificuldade;
    }

    public int getDificuldade() {
        return dificuldade;
    }

    @Override
    public int calcularXp() {
        return XP_PADRAO + XP_POR_NIVEL * dificuldade;
    }

    @Override
    public String explicarXp() {
        return XP_PADRAO + " XP + " + XP_POR_NIVEL + " XP × dificuldade " + dificuldade;
    }

    @Override
    public TipoConteudo tipo() {
        return TipoConteudo.DESAFIO;
    }

    @Override
    public String detalhe() {
        return "nível " + dificuldade + " de " + DIFICULDADE_MAXIMA;
    }
}
