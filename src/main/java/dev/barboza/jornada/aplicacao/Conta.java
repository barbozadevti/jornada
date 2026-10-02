package dev.barboza.jornada.aplicacao;

/** Credencial de acesso. O aluno aponta para o seu {@code Dev}; o coordenador não tem {@code devId}. */
public record Conta(String email, String senhaHash, Papel papel, String devId, String nome) {
}
