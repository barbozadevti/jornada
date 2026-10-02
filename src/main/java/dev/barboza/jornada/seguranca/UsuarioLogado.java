package dev.barboza.jornada.seguranca;

import java.io.Serializable;

import dev.barboza.jornada.aplicacao.Papel;
import dev.barboza.jornada.dominio.NaoEncontradoException;

/** Quem está na sessão. O aluno carrega o id do próprio dev; o coordenador não tem. */
public record UsuarioLogado(String email, String nome, Papel papel, String devId) implements Serializable {

    public boolean coordenador() {
        return papel == Papel.COORDENADOR;
    }

    /**
     * O aluno só mexe na própria jornada. Para o resto, o dev "não existe": 404 em vez de 403, para não
     * confirmar a existência de outras pessoas.
     */
    public void exigirProprio(String devId) {
        if (!coordenador() && !this.devId.equals(devId)) {
            throw new NaoEncontradoException("Dev não encontrado: " + devId);
        }
    }
}
