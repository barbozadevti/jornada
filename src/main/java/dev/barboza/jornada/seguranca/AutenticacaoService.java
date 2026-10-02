package dev.barboza.jornada.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import dev.barboza.jornada.aplicacao.Conta;
import dev.barboza.jornada.aplicacao.Escola;

/**
 * Confere e-mail e senha. Depois de 5 senhas erradas seguidas o e-mail fica bloqueado por 10 minutos,
 * e a mensagem é a mesma para e-mail inexistente e senha errada (não revela quem tem conta).
 */
@Service
public class AutenticacaoService {

    static final int LIMITE_DE_FALHAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(10);
    private static final String MENSAGEM = "E-mail ou senha incorretos.";

    private record Falhas(int quantidade, Instant desde) {
    }

    private final Escola escola;
    private final Clock relogio;
    private final Map<String, Falhas> falhas = new ConcurrentHashMap<>();

    public AutenticacaoService(Escola escola, Clock relogio) {
        this.escola = escola;
        this.relogio = relogio;
    }

    public UsuarioLogado autenticar(String email, String senha) {
        String chave = email == null ? "" : email.trim().toLowerCase();
        Instant agora = relogio.instant();
        Falhas anteriores = falhas.get(chave);
        if (anteriores != null && anteriores.quantidade() >= LIMITE_DE_FALHAS) {
            if (anteriores.desde().plus(BLOQUEIO).isAfter(agora)) {
                throw new CredenciaisInvalidasException("Muitas tentativas. Aguarde 10 minutos e tente de novo.");
            }
            falhas.remove(chave);
        }
        Conta conta = escola.conta(chave).filter(c -> escola.senhaConfere(c, senha)).orElse(null);
        if (conta == null) {
            falhas.merge(chave, new Falhas(1, agora),
                    (antes, novo) -> new Falhas(antes.quantidade() + 1, antes.quantidade() + 1 >= LIMITE_DE_FALHAS ? agora : antes.desde()));
            throw new CredenciaisInvalidasException(MENSAGEM);
        }
        falhas.remove(chave);
        return new UsuarioLogado(conta.email(), conta.nome(), conta.papel(), conta.devId());
    }
}
