package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.util.Optional;

/** Herança: uma mentoria vale o XP-base mais um adicional fixo e só pode ser concluída a partir do dia em que acontece. */
public final class Mentoria extends Conteudo {

    private static final int XP_ADICIONAL = 20;

    private final LocalDate data;

    public Mentoria(String id, String titulo, String descricao, LocalDate data) {
        super(id, titulo, descricao);
        if (data == null) {
            throw new RegraVioladaException("A mentoria precisa de uma data.");
        }
        this.data = data;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean jaAconteceu(LocalDate hoje) {
        return !hoje.isBefore(data);
    }

    @Override
    public Optional<String> impedimentoParaConcluir(LocalDate hoje) {
        return jaAconteceu(hoje)
                ? Optional.empty()
                : Optional.of("A mentoria só acontece em " + Datas.completa(data) + ".");
    }

    @Override
    public int calcularXp() {
        return XP_PADRAO + XP_ADICIONAL;
    }

    @Override
    public String explicarXp() {
        return XP_PADRAO + " XP + " + XP_ADICIONAL + " XP de mentoria";
    }

    @Override
    public TipoConteudo tipo() {
        return TipoConteudo.MENTORIA;
    }

    @Override
    public String detalhe() {
        return "dia " + Datas.curta(data);
    }
}
