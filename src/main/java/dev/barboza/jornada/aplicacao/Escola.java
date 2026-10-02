package dev.barboza.jornada.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.barboza.jornada.dominio.Bootcamp;
import dev.barboza.jornada.dominio.Certificado;
import dev.barboza.jornada.dominio.Conteudo;
import dev.barboza.jornada.dominio.Curso;
import dev.barboza.jornada.dominio.Desafio;
import dev.barboza.jornada.dominio.Dev;
import dev.barboza.jornada.dominio.EstadoInvalidoException;
import dev.barboza.jornada.dominio.Matricula;
import dev.barboza.jornada.dominio.Mentoria;
import dev.barboza.jornada.dominio.NaoEncontradoException;
import dev.barboza.jornada.dominio.RegraVioladaException;
import dev.barboza.jornada.dominio.SituacaoBootcamp;
import dev.barboza.jornada.dominio.TipoConteudo;

/**
 * Guarda bootcamps e devs e coordena os casos de uso. As regras ficam no domínio; aqui entram os identificadores,
 * o relógio e a persistência. Todas as operações são sincronizadas: o domínio é mutável e o servidor atende vários pedidos.
 */
public class Escola {

    /** Dados para criar um conteúdo; só os campos do tipo escolhido são lidos. */
    public record NovoConteudo(TipoConteudo tipo, String titulo, String descricao, Integer cargaHoraria,
                               LocalDate data, Integer dificuldade) {
    }

    /** O que aconteceu ao concluir um conteúdo. */
    public record Conclusao(Matricula matricula, Conteudo conteudo, Optional<Certificado> certificado) {
    }

    private final Clock relogio;
    private final Armazenamento armazenamento;
    private final Map<String, Bootcamp> bootcamps = new LinkedHashMap<>();
    private final Map<String, Dev> devs = new LinkedHashMap<>();
    private int proximoBootcamp = 1;
    private int proximoConteudo = 1;
    private int proximoDev = 1;

    public Escola(Clock relogio, Armazenamento armazenamento) {
        this.relogio = relogio;
        this.armazenamento = armazenamento;
    }

    public LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    // ---------- consultas ----------

    public synchronized List<Bootcamp> bootcamps() {
        return List.copyOf(bootcamps.values());
    }

    public synchronized Bootcamp bootcamp(String id) {
        return Optional.ofNullable(bootcamps.get(id))
                .orElseThrow(() -> new NaoEncontradoException("Bootcamp não encontrado: " + id));
    }

    public synchronized List<Dev> devs() {
        return List.copyOf(devs.values());
    }

    public synchronized Dev dev(String id) {
        return Optional.ofNullable(devs.get(id))
                .orElseThrow(() -> new NaoEncontradoException("Dev não encontrado: " + id));
    }

    /** Do maior XP para o menor; no empate, ordem alfabética. */
    public synchronized List<Dev> ranking() {
        return devs.values().stream()
                .sorted(Comparator.comparingInt(Dev::calcularTotalXp).reversed()
                        .thenComparing(Dev::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public synchronized Optional<Certificado> verificarCertificado(String codigo) {
        return devs.values().stream().flatMap(d -> d.certificados().stream())
                .filter(c -> c.codigo().equalsIgnoreCase(codigo == null ? "" : codigo.trim()))
                .findFirst();
    }

    // ---------- comandos ----------

    public synchronized Bootcamp criarBootcamp(String nome, String descricao, LocalDate dataInicial,
                                               int duracaoEmDias, int vagas) {
        Bootcamp bootcamp = criarBootcampEm(nome, descricao, dataInicial, duracaoEmDias, vagas, hoje());
        persistir();
        return bootcamp;
    }

    public synchronized Conteudo adicionarConteudo(String bootcampId, NovoConteudo novo) {
        Conteudo conteudo = fabricar(novo);
        bootcamp(bootcampId).adicionar(conteudo);
        persistir();
        return conteudo;
    }

    public synchronized Dev criarDev(String nome) {
        Dev dev = criarDevEm(nome);
        persistir();
        return dev;
    }

    public synchronized Matricula matricular(String devId, String bootcampId) {
        Matricula matricula = matricularEm(devId, bootcampId, hoje());
        persistir();
        return matricula;
    }

    public synchronized Conclusao progredir(String devId, String bootcampId) {
        Conclusao conclusao = progredirEm(devId, bootcampId, hoje());
        persistir();
        return conclusao;
    }

    /** Apaga tudo e recomeça com os dados de exemplo. */
    public synchronized void reiniciar() {
        limpar();
        Exemplos.preencher(this);
        persistir();
    }

    /** Carrega o que estava guardado; se não havia nada, começa com os dados de exemplo. */
    public synchronized void iniciar() {
        Optional<Estado> salvo = armazenamento.carregar();
        if (salvo.isPresent()) {
            try {
                restaurar(salvo.get());
                return;
            } catch (RuntimeException e) {
                limpar();
            }
        }
        Exemplos.preencher(this);
        persistir();
    }

    // ---------- operações com data explícita (usadas também pelos exemplos e pela restauração) ----------

    Bootcamp criarBootcampEm(String nome, String descricao, LocalDate dataInicial, int duracaoEmDias, int vagas,
                             LocalDate hoje) {
        Bootcamp bootcamp = new Bootcamp("b" + proximoBootcamp, nome, descricao, dataInicial, duracaoEmDias, vagas);
        if (bootcamp.situacao(hoje) == SituacaoBootcamp.ENCERRADO) {
            throw new RegraVioladaException("O bootcamp já teria terminado: a data final fica no passado.");
        }
        proximoBootcamp++;
        bootcamps.put(bootcamp.getId(), bootcamp);
        return bootcamp;
    }

    /** Usado só pelos exemplos, que contam a história de bootcamps que já terminaram. */
    Bootcamp criarBootcampHistorico(String nome, String descricao, LocalDate dataInicial, int duracaoEmDias, int vagas) {
        Bootcamp bootcamp = new Bootcamp("b" + proximoBootcamp++, nome, descricao, dataInicial, duracaoEmDias, vagas);
        bootcamps.put(bootcamp.getId(), bootcamp);
        return bootcamp;
    }

    Dev criarDevEm(String nome) {
        String limpo = nome == null ? "" : nome.trim();
        if (devs.values().stream().anyMatch(d -> d.getNome().equalsIgnoreCase(limpo))) {
            throw new EstadoInvalidoException("Já existe um dev chamado " + limpo + ".");
        }
        Dev dev = new Dev("d" + proximoDev, nome);
        proximoDev++;
        devs.put(dev.getId(), dev);
        return dev;
    }

    Matricula matricularEm(String devId, String bootcampId, LocalDate hoje) {
        return dev(devId).matricular(bootcamp(bootcampId), hoje);
    }

    Conclusao progredirEm(String devId, String bootcampId, LocalDate hoje) {
        Matricula matricula = dev(devId).matriculaEm(bootcampId)
                .orElseThrow(() -> new EstadoInvalidoException("Esse dev não está matriculado nesse bootcamp."));
        Conteudo conteudo = matricula.concluirProximo(hoje);
        return new Conclusao(matricula, conteudo, matricula.certificado());
    }

    Conteudo adicionarConteudoEm(Bootcamp bootcamp, NovoConteudo novo) {
        Conteudo conteudo = fabricar(novo);
        bootcamp.adicionar(conteudo);
        return conteudo;
    }

    // ---------- internos ----------

    private Conteudo fabricar(NovoConteudo novo) {
        if (novo == null || novo.tipo() == null) {
            throw new RegraVioladaException("Escolha o tipo do conteúdo: curso, mentoria ou desafio.");
        }
        String id = "c" + proximoConteudo;
        Conteudo conteudo = switch (novo.tipo()) {
            case CURSO -> new Curso(id, novo.titulo(), novo.descricao(), exigir(novo.cargaHoraria(), "a carga horária"));
            case MENTORIA -> new Mentoria(id, novo.titulo(), novo.descricao(), novo.data());
            case DESAFIO -> new Desafio(id, novo.titulo(), novo.descricao(), exigir(novo.dificuldade(), "a dificuldade"));
        };
        proximoConteudo++;
        return conteudo;
    }

    private static int exigir(Integer valor, String o) {
        if (valor == null) {
            throw new RegraVioladaException("Informe " + o + ".");
        }
        return valor;
    }

    private void limpar() {
        bootcamps.clear();
        devs.clear();
        proximoBootcamp = 1;
        proximoConteudo = 1;
        proximoDev = 1;
    }

    private void persistir() {
        armazenamento.salvar(exportar());
    }

    Estado exportar() {
        List<Estado.BootcampSalvo> salvosBootcamps = new ArrayList<>();
        for (Bootcamp b : bootcamps.values()) {
            List<Estado.ConteudoSalvo> conteudos = b.getConteudos().stream().map(c -> new Estado.ConteudoSalvo(
                    c.getId(), c.tipo(), c.getTitulo(), c.getDescricao(),
                    c instanceof Curso curso ? curso.getCargaHoraria() : null,
                    c instanceof Mentoria mentoria ? mentoria.getData() : null,
                    c instanceof Desafio desafio ? desafio.getDificuldade() : null)).toList();
            salvosBootcamps.add(new Estado.BootcampSalvo(b.getId(), b.getNome(), b.getDescricao(), b.getDataInicial(),
                    b.getDuracaoEmDias(), b.getVagas(), conteudos));
        }
        List<Estado.DevSalvo> salvosDevs = new ArrayList<>();
        for (Dev d : devs.values()) {
            List<Estado.MatriculaSalva> matriculas = d.getMatriculas().stream().map(m -> new Estado.MatriculaSalva(
                    m.getBootcamp().getId(), m.getInscritaEm(),
                    m.getConcluidos().entrySet().stream()
                            .map(e -> new Estado.ConclusaoSalva(e.getKey().getId(), e.getValue())).toList())).toList();
            salvosDevs.add(new Estado.DevSalvo(d.getId(), d.getNome(), matriculas));
        }
        return new Estado(proximoBootcamp, proximoConteudo, proximoDev, salvosBootcamps, salvosDevs);
    }

    private void restaurar(Estado estado) {
        limpar();
        proximoBootcamp = estado.proximoBootcamp();
        proximoConteudo = estado.proximoConteudo();
        proximoDev = estado.proximoDev();
        for (Estado.BootcampSalvo salvo : estado.bootcamps()) {
            Bootcamp bootcamp = new Bootcamp(salvo.id(), salvo.nome(), salvo.descricao(), salvo.dataInicial(),
                    salvo.duracaoEmDias(), salvo.vagas());
            for (Estado.ConteudoSalvo c : salvo.conteudos()) {
                bootcamp.adicionar(switch (c.tipo()) {
                    case CURSO -> new Curso(c.id(), c.titulo(), c.descricao(), c.cargaHoraria());
                    case MENTORIA -> new Mentoria(c.id(), c.titulo(), c.descricao(), c.data());
                    case DESAFIO -> new Desafio(c.id(), c.titulo(), c.descricao(), c.dificuldade());
                });
            }
            bootcamps.put(bootcamp.getId(), bootcamp);
        }
        for (Estado.DevSalvo salvo : estado.devs()) {
            Dev dev = new Dev(salvo.id(), salvo.nome());
            devs.put(dev.getId(), dev);
            for (Estado.MatriculaSalva m : salvo.matriculas()) {
                dev.matricular(bootcamp(m.bootcampId()), m.inscritaEm());
                for (Estado.ConclusaoSalva conclusao : m.conclusoes()) {
                    dev.matriculaEm(m.bootcampId()).orElseThrow().concluirProximo(conclusao.em());
                }
            }
        }
    }
}
