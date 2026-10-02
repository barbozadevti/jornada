package dev.barboza.jornada.dominio;

/** Herança: um curso vale {@code XP_PADRAO} por hora de carga horária. */
public final class Curso extends Conteudo {

    private final int cargaHoraria;

    public Curso(String id, String titulo, String descricao, int cargaHoraria) {
        super(id, titulo, descricao);
        if (cargaHoraria <= 0 || cargaHoraria > 200) {
            throw new RegraVioladaException("A carga horária do curso vai de 1 a 200 horas.");
        }
        this.cargaHoraria = cargaHoraria;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    @Override
    public int calcularXp() {
        return XP_PADRAO * cargaHoraria;
    }

    @Override
    public String explicarXp() {
        return XP_PADRAO + " XP × " + cargaHoraria + " h";
    }

    @Override
    public TipoConteudo tipo() {
        return TipoConteudo.CURSO;
    }

    @Override
    public String detalhe() {
        return cargaHoraria + " h";
    }
}
