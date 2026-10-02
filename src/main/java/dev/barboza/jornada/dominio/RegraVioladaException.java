package dev.barboza.jornada.dominio;

/** Pedido que fere uma regra do domínio (dado inválido, bootcamp lotado...). Vira HTTP 422. */
public class RegraVioladaException extends RuntimeException {

    public RegraVioladaException(String mensagem) {
        super(mensagem);
    }
}
