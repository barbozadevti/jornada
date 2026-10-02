package dev.barboza.jornada.aplicacao;

import java.time.LocalDate;
import java.util.List;

import dev.barboza.jornada.dominio.TipoConteudo;

/**
 * Retrato de tudo o que a escola guarda, no formato que vai para o arquivo JSON.
 * Ao carregar, o domínio é remontado chamando os mesmos métodos de sempre, com as datas originais,
 * então as regras valem também para o que veio do disco. Arquivos antigos, sem contas, ainda carregam.
 */
public record Estado(int proximoBootcamp, int proximoConteudo, int proximoDev,
                     List<BootcampSalvo> bootcamps, List<DevSalvo> devs, List<ContaSalva> contas) {

    public record BootcampSalvo(String id, String nome, String descricao, LocalDate dataInicial, int duracaoEmDias,
                                int vagas, List<ConteudoSalvo> conteudos) {
    }

    public record ConteudoSalvo(String id, TipoConteudo tipo, String titulo, String descricao, Integer cargaHoraria,
                                LocalDate data, Integer dificuldade) {
    }

    public record DevSalvo(String id, String nome, List<MatriculaSalva> matriculas) {
    }

    public record MatriculaSalva(String bootcampId, LocalDate inscritaEm, List<ConclusaoSalva> conclusoes) {
    }

    public record ConclusaoSalva(String conteudoId, LocalDate em) {
    }

    public record ContaSalva(String email, String senhaHash, Papel papel, String devId, String nome) {
    }
}
