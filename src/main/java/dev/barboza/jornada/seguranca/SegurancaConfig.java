package dev.barboza.jornada.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import dev.barboza.jornada.aplicacao.Papel;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sessão HTTP (cookie HttpOnly + SameSite) com proteção CSRF no padrão de SPA: o token vai num cookie legível
 * pelo JavaScript e volta no cabeçalho X-XSRF-TOKEN. O CSP e os demais cabeçalhos ficam em CabecalhosDeSeguranca.
 */
@Configuration
public class SegurancaConfig {

    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityContextRepository repositorioDeContexto() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain seguranca(HttpSecurity http, SecurityContextRepository contexto) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/img/**", "/health", "/health/**",
                                "/api/auth/entrar", "/api/auth/csrf",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Conferir um certificado é público: é para o recrutador que recebeu o código.
                        .requestMatchers(HttpMethod.GET, "/api/certificados/**").permitAll()
                        .requestMatchers("/api/demo/**").hasRole(Papel.COORDENADOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/bootcamps", "/api/bootcamps/**", "/api/devs")
                        .hasRole(Papel.COORDENADOR.name())
                        // O resto exige login; o aluno só enxerga e mexe no que é dele (checado nos controllers).
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.spa())
                .securityContext(s -> s.securityContextRepository(contexto))
                .sessionManagement(s -> s.sessionFixation(f -> f.changeSessionId()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                problema(res, HttpStatus.UNAUTHORIZED, "Não autenticado", "Faça login para continuar."))
                        .accessDeniedHandler((req, res, ex) ->
                                problema(res, HttpStatus.FORBIDDEN, "Acesso negado",
                                        "Seu perfil não permite esta operação (ou a sessão expirou: recarregue a página).")))
                .headers(h -> h.cacheControl(c -> c.disable()));
        return http.build();
    }

    private static void problema(HttpServletResponse res, HttpStatus status, String titulo, String detalhe)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"title\":\"" + titulo + "\",\"status\":" + status.value() + ",\"detail\":\"" + detalhe + "\"}");
    }
}
