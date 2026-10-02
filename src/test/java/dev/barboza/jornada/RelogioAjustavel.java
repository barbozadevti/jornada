package dev.barboza.jornada;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Relógio dos testes: parado em 2 de outubro de 2026, ao meio-dia, até alguém avançar. */
public class RelogioAjustavel extends Clock {

    public static final LocalDate HOJE = LocalDate.of(2026, 10, 2);
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Instant agora = HOJE.atTime(12, 0).atZone(ZONA).toInstant();

    public void avancarDias(int dias) {
        agora = agora.plus(Duration.ofDays(dias));
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}
