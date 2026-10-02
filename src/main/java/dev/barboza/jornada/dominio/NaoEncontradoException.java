package dev.barboza.jornada.dominio;

/** Bootcamp, dev ou certificado que não existe. Vira HTTP 404. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
