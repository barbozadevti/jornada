package dev.barboza.jornada;

import static dev.barboza.jornada.RelogioAjustavel.HOJE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.barboza.jornada.aplicacao.Armazenamento;
import dev.barboza.jornada.aplicacao.ArquivoJson;
import dev.barboza.jornada.aplicacao.Escola;
import dev.barboza.jornada.aplicacao.Escola.NovoConteudo;
import dev.barboza.jornada.dominio.EstadoInvalidoException;
import dev.barboza.jornada.dominio.NaoEncontradoException;
import dev.barboza.jornada.dominio.RegraVioladaException;
import dev.barboza.jornada.dominio.TipoConteudo;

class EscolaTest {

    private RelogioAjustavel relogio;
    private Escola escola;

    @BeforeEach
    void comecar() {
        relogio = new RelogioAjustavel();
        escola = new Escola(relogio, Armazenamento.emMemoria());
        escola.iniciar();
    }

    @Test
    void primeiraExecucaoTrazOsDadosDeExemplo() {
        assertThat(escola.bootcamps()).extracting("nome")
                .containsExactly("Java Developer", "Dados com SQL", "Fundamentos de Web");
        assertThat(escola.devs()).extracting("nome")
                .containsExactly("Camila Souza", "João Pereira", "Marina Alves", "Beatriz Lima");
    }

    @Test
    void osExemplosContamUmaHistoriaCoerente() {
        var camila = escola.dev("d1");
        assertThat(camila.getMatriculas()).hasSize(2);
        assertThat(camila.certificados()).hasSize(1);
        assertThat(camila.certificados().getFirst().bootcamp()).isEqualTo("Fundamentos de Web");
        assertThat(camila.calcularTotalXp()).isEqualTo(155 + 210);
        assertThat(escola.bootcamp("b1").getMatriculas()).hasSize(3);
    }

    @Test
    void rankingVaiDoMaiorXpParaOMenorEDesempataPorNome() {
        assertThat(escola.ranking()).extracting("nome")
                .containsExactly("Camila Souza", "João Pereira", "Marina Alves", "Beatriz Lima");
        escola.criarDev("Ana Zero");
        assertThat(escola.ranking()).extracting("nome").endsWith("Ana Zero", "Beatriz Lima");
    }

    @Test
    void fluxoCompletoDoCadastroAoCertificado() {
        var bootcamp = escola.criarBootcamp("Testes", "Bons testes", HOJE, 30, 10);
        escola.adicionarConteudo(bootcamp.getId(), new NovoConteudo(TipoConteudo.CURSO, "JUnit", "", 4, null, null));
        escola.adicionarConteudo(bootcamp.getId(), new NovoConteudo(TipoConteudo.DESAFIO, "Cobertura", "", null, null, 1));
        var dev = escola.criarDev("Nina");
        escola.matricular(dev.getId(), bootcamp.getId());
        assertThat(escola.progredir(dev.getId(), bootcamp.getId()).certificado()).isEmpty();
        var fim = escola.progredir(dev.getId(), bootcamp.getId());
        assertThat(fim.certificado()).isPresent();
        assertThat(dev.calcularTotalXp()).isEqualTo(40 + 25);
        assertThat(escola.verificarCertificado(fim.certificado().orElseThrow().codigo())).isPresent();
        assertThat(escola.verificarCertificado(" jrn-" + dev.getId() + "-" + bootcamp.getId() + " ")).isPresent();
    }

    @Test
    void validaOTipoEOsCamposDoConteudo() {
        assertThatThrownBy(() -> escola.adicionarConteudo("b1", new NovoConteudo(null, "A", "", 1, null, null)))
                .isInstanceOf(RegraVioladaException.class);
        var novo = escola.criarBootcamp("X", "", HOJE, 10, 5);
        assertThatThrownBy(() -> escola.adicionarConteudo(novo.getId(), new NovoConteudo(TipoConteudo.CURSO, "A", "", null, null, null)))
                .hasMessageContaining("carga horária");
        assertThatThrownBy(() -> escola.adicionarConteudo(novo.getId(), new NovoConteudo(TipoConteudo.MENTORIA, "A", "", null, null, null)))
                .hasMessageContaining("data");
    }

    @Test
    void naoCriaBootcampQueJaTerminou() {
        assertThatThrownBy(() -> escola.criarBootcamp("Velho", "", HOJE.minusDays(60), 30, 5))
                .isInstanceOf(RegraVioladaException.class).hasMessageContaining("já teria terminado");
    }

    @Test
    void nomeDeDevNaoRepete() {
        assertThatThrownBy(() -> escola.criarDev("camila souza")).isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void naoEncontradoDizOQueFalta() {
        assertThatThrownBy(() -> escola.bootcamp("b99")).isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> escola.dev("d99")).isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> escola.progredir("d4", "b1")).isInstanceOf(EstadoInvalidoException.class)
                .hasMessageContaining("não está matriculado");
    }

    @Test
    void osIdentificadoresNaoSaoReaproveitadosDepoisDeUmErro() {
        assertThatThrownBy(() -> escola.criarBootcamp("", "", HOJE, 10, 5)).isInstanceOf(RegraVioladaException.class);
        assertThat(escola.criarBootcamp("Bom", "", HOJE, 10, 5).getId()).isEqualTo("b4");
    }

    @Test
    void reiniciarVoltaAosExemplos() {
        escola.criarDev("Extra");
        escola.reiniciar();
        assertThat(escola.devs()).hasSize(4);
    }

    // ---------- persistência ----------

    @Test
    void oEstadoSobreviveAoReinicio(@TempDir Path pasta) {
        var arquivo = pasta.resolve("dados/jornada.json");
        var primeira = new Escola(relogio, new ArquivoJson(arquivo));
        primeira.iniciar();
        assertThat(arquivo).exists();
        var nina = primeira.criarDev("Nina Reis");
        primeira.matricular(nina.getId(), "b1");
        primeira.progredir(nina.getId(), "b1");

        var segunda = new Escola(relogio, new ArquivoJson(arquivo));
        segunda.iniciar();
        assertThat(segunda.devs()).hasSize(5);
        var restaurada = segunda.dev(nina.getId());
        assertThat(restaurada.calcularTotalXp()).isEqualTo(80);
        assertThat(restaurada.matriculaEm("b1").orElseThrow().proximo().orElseThrow().getTitulo()).isEqualTo("Orientação a objetos");
        assertThat(segunda.dev("d1").certificados()).hasSize(1);
        assertThat(segunda.ranking().stream().map(d -> d.getNome() + d.calcularTotalXp()).toList())
                .isEqualTo(primeira.ranking().stream().map(d -> d.getNome() + d.calcularTotalXp()).toList());
        // os identificadores continuam de onde pararam
        assertThat(segunda.criarDev("Outro").getId()).isEqualTo("d6");
    }

    @Test
    void arquivoQuebradoNaoImpedeOInicio(@TempDir Path pasta) throws Exception {
        var arquivo = pasta.resolve("jornada.json");
        Files.writeString(arquivo, "{ isto não é json");
        var nova = new Escola(relogio, new ArquivoJson(arquivo));
        nova.iniciar();
        assertThat(nova.bootcamps()).hasSize(3);
        assertThat(Files.readString(arquivo)).contains("Java Developer");
    }
}
