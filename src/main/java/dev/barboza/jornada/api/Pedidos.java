package dev.barboza.jornada.api;

import java.time.LocalDate;

import dev.barboza.jornada.dominio.TipoConteudo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** O formato dos corpos recebidos pela API. Os limites finos (1 a 200 horas...) ficam no domínio. */
public final class Pedidos {

    private Pedidos() {
    }

    public record NovoBootcamp(@NotBlank @Size(max = 60) String nome, @Size(max = 300) String descricao,
                               @NotNull LocalDate dataInicial, @NotNull Integer duracaoEmDias, @NotNull Integer vagas) {
    }

    public record NovoConteudo(@NotNull TipoConteudo tipo, @NotBlank @Size(max = 80) String titulo,
                               @Size(max = 300) String descricao, Integer cargaHoraria, LocalDate data,
                               Integer dificuldade) {
    }

    public record NovoDev(@NotBlank @Size(max = 50) String nome, @NotBlank @Size(max = 120) String email,
                          @NotBlank @Size(max = 72) String senha) {
    }

    public record Credenciais(@NotBlank @Size(max = 120) String email, @NotBlank @Size(max = 72) String senha) {
    }

    public record NovaMatricula(@NotBlank String bootcampId) {
    }
}
