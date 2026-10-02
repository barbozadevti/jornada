package dev.barboza.jornada.seguranca;

/** E-mail ou senha errados, ou conta bloqueada por tentativas demais. Vira HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
