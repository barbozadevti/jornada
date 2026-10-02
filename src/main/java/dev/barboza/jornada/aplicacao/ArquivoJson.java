package dev.barboza.jornada.aplicacao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Guarda o estado em um arquivo JSON, gravado em um temporário e movido para o lugar (nunca fica pela metade). */
public class ArquivoJson implements Armazenamento {

    private static final Logger log = LoggerFactory.getLogger(ArquivoJson.class);
    private static final JsonMapper JSON = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    private final Path arquivo;

    public ArquivoJson(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public Optional<Estado> carregar() {
        if (!Files.isRegularFile(arquivo)) {
            return Optional.empty();
        }
        try {
            return Optional.of(JSON.readValue(Files.readString(arquivo), Estado.class));
        } catch (IOException | RuntimeException e) {
            log.warn("Não consegui ler {}; começando do zero ({}).", arquivo, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void salvar(Estado estado) {
        try {
            Files.createDirectories(arquivo.toAbsolutePath().getParent());
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            Files.writeString(temporario, JSON.writeValueAsString(estado));
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar " + arquivo, e);
        }
    }
}
