package dev.barboza.jornada.dominio;

/** Ação que não cabe no estado atual (inscrição repetida, mentoria que ainda não aconteceu...). Vira HTTP 409. */
public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
