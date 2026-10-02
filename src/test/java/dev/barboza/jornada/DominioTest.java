package dev.barboza.jornada;

import static dev.barboza.jornada.RelogioAjustavel.HOJE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import dev.barboza.jornada.dominio.Bootcamp;
import dev.barboza.jornada.dominio.Conteudo;
import dev.barboza.jornada.dominio.Curso;
import dev.barboza.jornada.dominio.Desafio;
import dev.barboza.jornada.dominio.Dev;
import dev.barboza.jornada.dominio.EstadoInvalidoException;
import dev.barboza.jornada.dominio.Matricula;
import dev.barboza.jornada.dominio.Mentoria;
import dev.barboza.jornada.dominio.NivelDev;
import dev.barboza.jornada.dominio.RegraVioladaException;
import dev.barboza.jornada.dominio.SituacaoBootcamp;

class DominioTest {

    private static Bootcamp bootcamp(int vagas) {
        return new Bootcamp("b1", "Java", "", HOJE.minusDays(1), 30, vagas)
                .adicionar(new Curso("c1", "Java básico", "", 8))
                .adicionar(new Mentoria("c2", "Mentoria", "", HOJE))
                .adicionar(new Desafio("c3", "Projeto", "", 3));
    }

    // ---------- abstração, herança e polimorfismo ----------

    @Test
    void conteudoEhAbstratoEFechado() {
        assertThat(Modifier.isAbstract(Conteudo.class.getModifiers())).isTrue();
        assertThat(Conteudo.class.isSealed()).isTrue();
        assertThat(Conteudo.class.getPermittedSubclasses()).containsExactlyInAnyOrder(Curso.class, Mentoria.class, Desafio.class);
    }

    @Test
    void cadaTipoCalculaOXpDoSeuJeito() {
        assertThat(new Curso("c", "A", "", 8).calcularXp()).isEqualTo(80);
        assertThat(new Mentoria("c", "A", "", HOJE).calcularXp()).isEqualTo(30);
        assertThat(new Desafio("c", "A", "", 1).calcularXp()).isEqualTo(25);
        assertThat(new Desafio("c", "A", "", 3).calcularXp()).isEqualTo(55);
    }

    @Test
    void oXpEhExplicadoEmTexto() {
        assertThat(new Curso("c", "A", "", 8).explicarXp()).isEqualTo("10 XP × 8 h");
        assertThat(new Mentoria("c", "A", "", HOJE).explicarXp()).isEqualTo("10 XP + 20 XP de mentoria");
        assertThat(new Desafio("c", "A", "", 2).explicarXp()).isEqualTo("10 XP + 15 XP × dificuldade 2");
    }

    @Test
    void omesmoCodigoSomaOXpDeQualquerConteudo() {
        List<Conteudo> todos = bootcamp(5).getConteudos();
        assertThat(todos.stream().mapToInt(Conteudo::calcularXp).sum()).isEqualTo(165);
        assertThat(bootcamp(5).calcularXpTotal()).isEqualTo(165);
    }

    @Test
    void detalheDeCadaTipo() {
        assertThat(new Curso("c", "A", "", 8).detalhe()).isEqualTo("8 h");
        assertThat(new Mentoria("c", "A", "", LocalDate.of(2026, 10, 9)).detalhe()).isEqualTo("dia 09/10");
        assertThat(new Desafio("c", "A", "", 2).detalhe()).isEqualTo("nível 2 de 3");
    }

    // ---------- encapsulamento ----------

    @Test
    void colecoesExpostasSaoSomenteLeitura() {
        Bootcamp b = bootcamp(5);
        assertThatThrownBy(() -> b.getConteudos().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> b.getMatriculas().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new Dev("d1", "Ana").getMatriculas().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void naoHaSettersPublicos() {
        for (Class<?> tipo : List.of(Conteudo.class, Curso.class, Mentoria.class, Desafio.class, Bootcamp.class, Dev.class, Matricula.class)) {
            assertThat(List.of(tipo.getDeclaredMethods())).as(tipo.getSimpleName())
                    .noneMatch(m -> Modifier.isPublic(m.getModifiers()) && m.getName().startsWith("set"));
        }
    }

    @Test
    void recusaDadosInvalidos() {
        assertThatThrownBy(() -> new Curso("c", " ", "", 4)).isInstanceOf(RegraVioladaException.class).hasMessageContaining("título");
        assertThatThrownBy(() -> new Curso("c", "A", "", 0)).hasMessageContaining("carga horária");
        assertThatThrownBy(() -> new Curso("c", "A", "", 201)).hasMessageContaining("carga horária");
        assertThatThrownBy(() -> new Desafio("c", "A", "", 4)).hasMessageContaining("1 a 3");
        assertThatThrownBy(() -> new Mentoria("c", "A", "", null)).hasMessageContaining("data");
        assertThatThrownBy(() -> new Bootcamp("b", "A", "", HOJE, 10, 0)).hasMessageContaining("vagas");
        assertThatThrownBy(() -> new Bootcamp("b", "A", "", HOJE, 0, 5)).hasMessageContaining("duração");
        assertThatThrownBy(() -> new Bootcamp("b", " ", "", HOJE, 10, 5)).hasMessageContaining("nome");
        assertThatThrownBy(() -> new Dev("d", " ")).hasMessageContaining("nome");
    }

    // ---------- regras do bootcamp ----------

    @Test
    void trilhaNaoAceitaTituloRepetido() {
        assertThatThrownBy(() -> bootcamp(5).adicionar(new Curso("c9", "java BÁSICO", "", 2)))
                .isInstanceOf(EstadoInvalidoException.class).hasMessageContaining("já está na trilha");
    }

    @Test
    void trilhaFicaCongeladaDepoisDaPrimeiraMatricula() {
        Bootcamp b = bootcamp(5);
        new Dev("d1", "Ana").matricular(b, HOJE);
        assertThatThrownBy(() -> b.adicionar(new Curso("c9", "Novo", "", 2)))
                .isInstanceOf(EstadoInvalidoException.class).hasMessageContaining("depois que há alunos");
    }

    @Test
    void situacaoDependeDaData() {
        Bootcamp b = new Bootcamp("b", "A", "", HOJE, 10, 5);
        assertThat(b.situacao(HOJE.minusDays(1))).isEqualTo(SituacaoBootcamp.PROXIMO);
        assertThat(b.situacao(HOJE)).isEqualTo(SituacaoBootcamp.EM_ANDAMENTO);
        assertThat(b.situacao(HOJE.plusDays(10))).isEqualTo(SituacaoBootcamp.EM_ANDAMENTO);
        assertThat(b.situacao(HOJE.plusDays(11))).isEqualTo(SituacaoBootcamp.ENCERRADO);
    }

    // ---------- matrícula e progresso ----------

    @Test
    void recusaMatriculaRepetidaLotadaEmBootcampEncerradoOuSemTrilha() {
        Bootcamp b = bootcamp(1);
        Dev ana = new Dev("d1", "Ana");
        ana.matricular(b, HOJE);
        assertThatThrownBy(() -> ana.matricular(b, HOJE)).hasMessageContaining("já está matriculado");
        assertThatThrownBy(() -> new Dev("d2", "Bia").matricular(b, HOJE)).hasMessageContaining("não tem mais vagas");
        assertThatThrownBy(() -> new Dev("d3", "Caio").matricular(bootcamp(5), HOJE.plusDays(60))).hasMessageContaining("encerrado");
        assertThatThrownBy(() -> new Dev("d4", "Duda").matricular(new Bootcamp("b9", "Vazio", "", HOJE, 10, 5), HOJE))
                .hasMessageContaining("ainda não tem conteúdos");
    }

    @Test
    void progredirConcluiNaOrdemDaTrilhaESomaOXp() {
        Dev ana = new Dev("d1", "Ana");
        Matricula m = ana.matricular(bootcamp(5), HOJE);
        assertThat(m.proximo().orElseThrow().getTitulo()).isEqualTo("Java básico");
        assertThat(m.concluirProximo(HOJE).getTitulo()).isEqualTo("Java básico");
        assertThat(ana.calcularTotalXp()).isEqualTo(80);
        assertThat(m.percentual()).isEqualTo(33);
        m.concluirProximo(HOJE);
        m.concluirProximo(HOJE);
        assertThat(ana.calcularTotalXp()).isEqualTo(165);
        assertThat(m.percentual()).isEqualTo(100);
        assertThat(m.concluida()).isTrue();
        assertThatThrownBy(() -> m.concluirProximo(HOJE)).hasMessageContaining("já concluiu todos");
    }

    @Test
    void mentoriaSoConcluiAPartirDeSuaData() {
        Bootcamp b = new Bootcamp("b", "A", "", HOJE, 30, 5).adicionar(new Mentoria("c", "Futura", "", HOJE.plusDays(3)));
        Matricula m = new Dev("d1", "Ana").matricular(b, HOJE);
        assertThat(m.impedimento(HOJE)).contains("A mentoria só acontece em 05/10/2026.");
        assertThatThrownBy(() -> m.concluirProximo(HOJE)).isInstanceOf(EstadoInvalidoException.class);
        assertThat(m.getDev().calcularTotalXp()).isZero();
        m.concluirProximo(HOJE.plusDays(3));
        assertThat(m.getDev().calcularTotalXp()).isEqualTo(30);
    }

    @Test
    void naoProgrideAntesDoInicioNemDepoisDoFim() {
        Bootcamp b = new Bootcamp("b", "A", "", HOJE.plusDays(5), 10, 5).adicionar(new Curso("c", "A", "", 1))
                .adicionar(new Curso("c2", "B", "", 1));
        Matricula m = new Dev("d1", "Ana").matricular(b, HOJE);
        assertThatThrownBy(() -> m.concluirProximo(HOJE)).hasMessageContaining("só começa em 07/10/2026");
        m.concluirProximo(HOJE.plusDays(6));
        assertThatThrownBy(() -> m.concluirProximo(HOJE.plusDays(20))).hasMessageContaining("já foi encerrado");
    }

    @Test
    void certificadoSaiAoConcluirATrilhaEUsaODiaDaUltimaConclusao() {
        Dev ana = new Dev("d1", "Ana");
        Matricula m = ana.matricular(bootcamp(5), HOJE);
        m.concluirProximo(HOJE);
        m.concluirProximo(HOJE);
        assertThat(m.certificado()).isEmpty();
        m.concluirProximo(HOJE.plusDays(2));
        var certificado = m.certificado().orElseThrow();
        assertThat(certificado.codigo()).isEqualTo("JRN-D1-B1");
        assertThat(certificado.emitidoEm()).isEqualTo(HOJE.plusDays(2));
        assertThat(certificado.xp()).isEqualTo(165);
        assertThat(ana.certificados()).containsExactly(certificado);
    }

    @Test
    void devPodeEstarEmMaisDeUmBootcamp() {
        Dev ana = new Dev("d1", "Ana");
        ana.matricular(bootcamp(5), HOJE);
        ana.matricular(new Bootcamp("b2", "SQL", "", HOJE, 30, 5).adicionar(new Curso("c9", "SQL", "", 4)), HOJE);
        assertThat(ana.getMatriculas()).hasSize(2);
        assertThat(ana.matriculaEm("b2")).isPresent();
        assertThat(ana.matriculaEm("b9")).isEmpty();
    }

    // ---------- nível ----------

    @ParameterizedTest
    @CsvSource({"0,INICIANTE", "149,INICIANTE", "150,JUNIOR", "399,JUNIOR", "400,PLENO", "799,PLENO", "800,SENIOR", "5000,SENIOR"})
    void nivelDependeSoDoXp(int xp, NivelDev esperado) {
        assertThat(NivelDev.paraXp(xp)).isEqualTo(esperado);
    }

    @Test
    void proximoNivel() {
        assertThat(NivelDev.INICIANTE.proximo()).contains(NivelDev.JUNIOR);
        assertThat(NivelDev.SENIOR.proximo()).isEmpty();
    }
}
