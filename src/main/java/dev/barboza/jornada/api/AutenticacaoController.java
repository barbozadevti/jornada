package dev.barboza.jornada.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.barboza.jornada.api.Pedidos.Credenciais;
import dev.barboza.jornada.seguranca.AutenticacaoService;
import dev.barboza.jornada.seguranca.UsuarioLogado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login por sessão (cookie) com proteção CSRF. Contas de demonstração: veja o README.")
public class AutenticacaoController {

    private final AutenticacaoService autenticacao;
    private final SecurityContextRepository contextos;

    public AutenticacaoController(AutenticacaoService autenticacao, SecurityContextRepository contextos) {
        this.autenticacao = autenticacao;
        this.contextos = contextos;
    }

    public record Sessao(String nome, String email, String papel, String papelRotulo, String devId) {
        static Sessao de(UsuarioLogado u) {
            return new Sessao(u.nome(), u.email(), u.papel().name(), u.papel().rotulo(), u.devId());
        }
    }

    @Operation(summary = "Entrar", description = "5 senhas erradas seguidas bloqueiam o e-mail por 10 minutos.")
    @PostMapping("/entrar")
    public Sessao entrar(@Valid @RequestBody Credenciais credenciais, HttpServletRequest req, HttpServletResponse res) {
        UsuarioLogado usuario = autenticacao.autenticar(credenciais.email(), credenciais.senha());
        var token = UsernamePasswordAuthenticationToken.authenticated(usuario, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.papel().name())));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(token);
        SecurityContextHolder.setContext(contexto);
        req.getSession(true);
        req.changeSessionId();
        contextos.saveContext(contexto, req, res);
        return Sessao.de(usuario);
    }

    @Operation(summary = "Quem está logado")
    @GetMapping("/eu")
    public Sessao eu(@AuthenticationPrincipal UsuarioLogado usuario) {
        return Sessao.de(usuario);
    }

    @Operation(summary = "Sair", description = "Invalida a sessão.")
    @PostMapping("/sair")
    public ResponseEntity<Void> sair(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Garante o cookie XSRF-TOKEN antes do primeiro POST (o site chama ao abrir). */
    @Operation(summary = "Token CSRF")
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
