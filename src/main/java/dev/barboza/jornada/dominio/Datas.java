package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formatos de data das mensagens do domínio. */
final class Datas {

    private static final DateTimeFormatter COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter CURTA = DateTimeFormatter.ofPattern("dd/MM");

    private Datas() {
    }

    static String completa(LocalDate data) {
        return data.format(COMPLETA);
    }

    static String curta(LocalDate data) {
        return data.format(CURTA);
    }
}
