package dev.barboza.jornada.config;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.barboza.jornada.aplicacao.Armazenamento;
import dev.barboza.jornada.aplicacao.ArquivoJson;
import dev.barboza.jornada.aplicacao.Escola;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class Configuracao {

    private static final Logger log = LoggerFactory.getLogger(Configuracao.class);

    /** Relógio injetável: os testes trocam por um relógio que só anda quando mandam. */
    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    /** Com {@code jornada.arquivo} preenchido o estado sobrevive ao reinício; vazio, vive só na memória. */
    @Bean
    Armazenamento armazenamento(@Value("${jornada.arquivo:}") String arquivo) {
        if (arquivo.isBlank()) {
            return Armazenamento.emMemoria();
        }
        Path caminho = Path.of(arquivo.replace("~", System.getProperty("user.home")));
        log.info("Dados da Jornada em {}", caminho.toAbsolutePath());
        return new ArquivoJson(caminho);
    }

    @Bean(initMethod = "iniciar")
    Escola escola(Clock relogio, Armazenamento armazenamento, PasswordEncoder codificador) {
        return new Escola(relogio, armazenamento, codificador);
    }

    @Bean
    OpenAPI documentacao() {
        return new OpenAPI().info(new Info()
                .title("Jornada — API de bootcamps")
                .version("1.0")
                .description("Bootcamps com trilha de conteúdos (curso, mentoria, desafio), matrícula de devs, "
                        + "progresso com XP, ranking e certificados."));
    }

    /** Usado pelo atalho: quando o servidor fica pronto, abre o site no navegador padrão (Windows). */
    @EventListener
    public void aoFicarPronto(ApplicationReadyEvent evento) {
        Environment ambiente = evento.getApplicationContext().getEnvironment();
        if (!ambiente.getProperty("jornada.abrir-navegador", Boolean.class, false)
                || !(evento.getApplicationContext() instanceof WebServerApplicationContext web)) {
            return;
        }
        String endereco = "http://localhost:" + web.getWebServer().getPort();
        try {
            new ProcessBuilder("cmd", "/c", "start", "", endereco).start();
        } catch (IOException e) {
            log.warn("Não foi possível abrir o navegador; acesse {}", endereco);
        }
    }
}
