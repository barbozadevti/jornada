package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A ligação entre um dev e um bootcamp: guarda a trilha percorrida, na ordem, com o dia de cada conclusão.
 * Concluir o último conteúdo emite o certificado.
 */
public class Matricula {

    private final Dev dev;
    private final Bootcamp bootcamp;
    private final LocalDate inscritaEm;
    private final Map<Conteudo, LocalDate> concluidos = new LinkedHashMap<>();

    Matricula(Dev dev, Bootcamp bootcamp, LocalDate inscritaEm) {
        this.dev = dev;
        this.bootcamp = bootcamp;
        this.inscritaEm = inscritaEm;
    }

    /** O próximo conteúdo da trilha que ainda não foi concluído. */
    public Optional<Conteudo> proximo() {
        return bootcamp.getConteudos().stream().filter(c -> !concluidos.containsKey(c)).findFirst();
    }

    /** Por que não dá para concluir o próximo conteúdo hoje; vazio se dá (ou se não há mais o que concluir). */
    public Optional<String> impedimento(LocalDate hoje) {
        if (hoje.isBefore(bootcamp.getDataInicial())) {
            return Optional.of("O bootcamp só começa em " + Datas.completa(bootcamp.getDataInicial()) + ".");
        }
        if (bootcamp.situacao(hoje) == SituacaoBootcamp.ENCERRADO && proximo().isPresent()) {
            return Optional.of("O bootcamp já foi encerrado.");
        }
        return proximo().flatMap(c -> c.impedimentoParaConcluir(hoje));
    }

    /** Conclui o próximo conteúdo da trilha. Cada tipo de conteúdo decide se já pode ser concluído (polimorfismo). */
    public Conteudo concluirProximo(LocalDate hoje) {
        Conteudo proximo = proximo().orElseThrow(() ->
                new EstadoInvalidoException("Você já concluiu todos os conteúdos de " + bootcamp.getNome() + "."));
        impedimento(hoje).ifPresent(motivo -> {
            throw new EstadoInvalidoException(motivo);
        });
        concluidos.put(proximo, hoje);
        return proximo;
    }

    public boolean concluida() {
        return proximo().isEmpty();
    }

    public int xpGanho() {
        return concluidos.keySet().stream().mapToInt(Conteudo::calcularXp).sum();
    }

    public int percentual() {
        int total = bootcamp.getConteudos().size();
        return total == 0 ? 0 : concluidos.size() * 100 / total;
    }

    public Optional<Certificado> certificado() {
        if (!concluida()) {
            return Optional.empty();
        }
        LocalDate ultimo = concluidos.values().stream().max(LocalDate::compareTo).orElse(inscritaEm);
        return Optional.of(new Certificado(codigoDoCertificado(dev, bootcamp), dev.getNome(), bootcamp.getNome(), ultimo, xpGanho()));
    }

    static String codigoDoCertificado(Dev dev, Bootcamp bootcamp) {
        return ("JRN-" + dev.getId() + "-" + bootcamp.getId()).toUpperCase();
    }

    public Dev getDev() {
        return dev;
    }

    public Bootcamp getBootcamp() {
        return bootcamp;
    }

    public LocalDate getInscritaEm() {
        return inscritaEm;
    }

    public Map<Conteudo, LocalDate> getConcluidos() {
        return Collections.unmodifiableMap(concluidos);
    }
}
