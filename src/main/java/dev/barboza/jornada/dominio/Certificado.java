package dev.barboza.jornada.dominio;

import java.time.LocalDate;

/** Emitido quando o dev conclui todos os conteúdos de um bootcamp. O código permite conferir a autenticidade. */
public record Certificado(String codigo, String dev, String bootcamp, LocalDate emitidoEm, int xp) {
}
