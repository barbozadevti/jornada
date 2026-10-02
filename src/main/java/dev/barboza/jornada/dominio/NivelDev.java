package dev.barboza.jornada.dominio;

import java.util.Optional;

/** O nível do dev depende só do XP acumulado. */
public enum NivelDev {
    INICIANTE("Iniciante", 0),
    JUNIOR("Júnior", 150),
    PLENO("Pleno", 400),
    SENIOR("Sênior", 800);

    private final String rotulo;
    private final int xpMinimo;

    NivelDev(String rotulo, int xpMinimo) {
        this.rotulo = rotulo;
        this.xpMinimo = xpMinimo;
    }

    public static NivelDev paraXp(int xp) {
        NivelDev nivel = INICIANTE;
        for (NivelDev candidato : values()) {
            if (xp >= candidato.xpMinimo) {
                nivel = candidato;
            }
        }
        return nivel;
    }

    public Optional<NivelDev> proximo() {
        int indice = ordinal() + 1;
        return indice < values().length ? Optional.of(values()[indice]) : Optional.empty();
    }

    public String rotulo() {
        return rotulo;
    }

    public int xpMinimo() {
        return xpMinimo;
    }
}
