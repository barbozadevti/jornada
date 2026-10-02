package dev.barboza.jornada.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Quem faz os bootcamps. O XP e o nível vêm das matrículas, nunca são guardados à parte. */
public class Dev {

    private final String id;
    private final String nome;
    private final List<Matricula> matriculas = new ArrayList<>();

    public Dev(String id, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("O dev precisa de um nome.");
        }
        if (nome.trim().length() > 50) {
            throw new RegraVioladaException("O nome do dev pode ter até 50 caracteres.");
        }
        this.id = id;
        this.nome = nome.trim();
    }

    public Matricula matricular(Bootcamp bootcamp, LocalDate hoje) {
        if (bootcamp.situacao(hoje) == SituacaoBootcamp.ENCERRADO) {
            throw new EstadoInvalidoException("O bootcamp " + bootcamp.getNome() + " já foi encerrado.");
        }
        if (bootcamp.getConteudos().isEmpty()) {
            throw new EstadoInvalidoException("O bootcamp " + bootcamp.getNome() + " ainda não tem conteúdos.");
        }
        if (matriculaEm(bootcamp.getId()).isPresent()) {
            throw new EstadoInvalidoException(nome + " já está matriculado em " + bootcamp.getNome() + ".");
        }
        if (!bootcamp.temVaga()) {
            throw new EstadoInvalidoException("O bootcamp " + bootcamp.getNome() + " não tem mais vagas.");
        }
        Matricula matricula = new Matricula(this, bootcamp, hoje);
        matriculas.add(matricula);
        bootcamp.registrar(matricula);
        return matricula;
    }

    public Optional<Matricula> matriculaEm(String bootcampId) {
        return matriculas.stream().filter(m -> m.getBootcamp().getId().equals(bootcampId)).findFirst();
    }

    /** Soma o XP de tudo o que foi concluído; cada conteúdo usa a sua própria regra (polimorfismo). */
    public int calcularTotalXp() {
        return matriculas.stream().mapToInt(Matricula::xpGanho).sum();
    }

    public NivelDev nivel() {
        return NivelDev.paraXp(calcularTotalXp());
    }

    public List<Certificado> certificados() {
        return matriculas.stream().flatMap(m -> m.certificado().stream()).toList();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<Matricula> getMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }
}
