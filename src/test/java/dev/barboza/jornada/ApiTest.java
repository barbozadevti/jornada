package dev.barboza.jornada;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiTest.Relogio.class)
class ApiTest {

    @TestConfiguration
    static class Relogio {
        @Bean
        @Primary
        RelogioAjustavel relogioAjustavel() {
            return new RelogioAjustavel();
        }
    }

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void dadosDeExemplo() throws Exception {
        mvc.perform(post("/api/demo/reiniciar")).andExpect(status().isNoContent());
    }

    private ResultActions postar(String rota, String json) throws Exception {
        return mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ---------- leitura ----------

    @Test
    void listaOsBootcampsComSituacaoVagasEXp() throws Exception {
        mvc.perform(get("/api/bootcamps")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome", is("Java Developer")))
                .andExpect(jsonPath("$[0].situacao", is("EM_ANDAMENTO")))
                .andExpect(jsonPath("$[0].xpTotal", is(395)))
                .andExpect(jsonPath("$[0].matriculados", is(3)))
                .andExpect(jsonPath("$[0].vagasRestantes", is(37)))
                .andExpect(jsonPath("$[0].trilhaCongelada", is(true)))
                .andExpect(jsonPath("$[1].situacao", is("PROXIMO")))
                .andExpect(jsonPath("$[2].situacao", is("ENCERRADO")));
    }

    @Test
    void mostraATrilhaComOXpDeCadaTipoExplicado() throws Exception {
        mvc.perform(get("/api/bootcamps/b1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudos", hasSize(7)))
                .andExpect(jsonPath("$.conteudos[0].tipo", is("CURSO")))
                .andExpect(jsonPath("$.conteudos[0].xp", is(80)))
                .andExpect(jsonPath("$.conteudos[0].explicacaoXp", is("10 XP × 8 h")))
                .andExpect(jsonPath("$.conteudos[2].tipo", is("MENTORIA")))
                .andExpect(jsonPath("$.conteudos[2].xp", is(30)))
                .andExpect(jsonPath("$.conteudos[3].tipo", is("DESAFIO")))
                .andExpect(jsonPath("$.conteudos[3].detalhe", is("nível 2 de 3")));
    }

    @Test
    void mostraOProgressoDeUmDev() throws Exception {
        mvc.perform(get("/api/devs/d1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Camila Souza")))
                .andExpect(jsonPath("$.xp", is(365)))
                .andExpect(jsonPath("$.nivel.nome", is("Júnior")))
                .andExpect(jsonPath("$.nivel.proximoNome", is("Pleno")))
                .andExpect(jsonPath("$.certificados", is(1)))
                .andExpect(jsonPath("$.matriculas", hasSize(2)))
                .andExpect(jsonPath("$.matriculas[1].bootcampNome", is("Java Developer")))
                .andExpect(jsonPath("$.matriculas[1].concluidos", is(4)))
                .andExpect(jsonPath("$.matriculas[1].percentual", is(57)))
                .andExpect(jsonPath("$.matriculas[1].proximo.titulo", is("Spring Boot e APIs REST")))
                .andExpect(jsonPath("$.matriculas[1].podeProgredir", is(true)))
                .andExpect(jsonPath("$.matriculas[1].trilha[0].estado", is("CONCLUIDO")))
                .andExpect(jsonPath("$.matriculas[1].trilha[4].estado", is("PROXIMO")))
                .andExpect(jsonPath("$.matriculas[1].trilha[5].estado", is("PENDENTE")))
                .andExpect(jsonPath("$.matriculas[0].certificado.codigo", is("JRN-D1-B3")));
    }

    @Test
    void rankingOrdenaPorXp() throws Exception {
        mvc.perform(get("/api/ranking")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].posicao", is(1)))
                .andExpect(jsonPath("$[0].nome", is("Camila Souza")))
                .andExpect(jsonPath("$[0].xp", is(365)))
                .andExpect(jsonPath("$[3].nome", is("Beatriz Lima")))
                .andExpect(jsonPath("$[3].xp", is(0)));
    }

    @Test
    void regrasDeXpDosTresTipos() throws Exception {
        mvc.perform(get("/api/regras-xp")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].exemplo", is("10 XP × 8 h = 80 XP")))
                .andExpect(jsonPath("$[1].exemplo", is("10 XP + 20 XP de mentoria = 30 XP")))
                .andExpect(jsonPath("$[2].exemplo", is("10 XP + 15 XP × dificuldade 2 = 40 XP")));
    }

    // ---------- comandos ----------

    @Test
    void criaBootcampMontaATrilhaMatriculaEEmiteCertificado() throws Exception {
        postar("/api/bootcamps", """
                {"nome":"Testes automatizados","descricao":"JUnit na prática","dataInicial":"2026-10-02","duracaoEmDias":30,"vagas":5}""")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id", is("b4")))
                .andExpect(jsonPath("$.trilhaCongelada", is(false)));
        postar("/api/bootcamps/b4/conteudos", """
                {"tipo":"CURSO","titulo":"JUnit 5","cargaHoraria":4}""")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.xp", is(40)));
        postar("/api/bootcamps/b4/conteudos", """
                {"tipo":"MENTORIA","titulo":"Revisão","data":"2026-10-02"}""")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.xp", is(30)));
        postar("/api/devs", """
                {"nome":"Nina Reis"}""").andExpect(status().isCreated()).andExpect(jsonPath("$.id", is("d5")));
        postar("/api/devs/d5/matriculas", """
                {"bootcampId":"b4"}""").andExpect(status().isCreated())
                .andExpect(jsonPath("$.matriculas[0].total", is(2)));

        postar("/api/devs/d5/matriculas/b4/progresso", "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.concluido.titulo", is("JUnit 5")))
                .andExpect(jsonPath("$.xpGanho", is(40)))
                .andExpect(jsonPath("$.certificadoEmitido", is(false)));
        postar("/api/devs/d5/matriculas/b4/progresso", "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.certificadoEmitido", is(true)))
                .andExpect(jsonPath("$.dev.xp", is(70)))
                .andExpect(jsonPath("$.dev.matriculas[0].certificado.codigo", is("JRN-D5-B4")));

        mvc.perform(get("/api/certificados/JRN-D5-B4")).andExpect(status().isOk())
                .andExpect(jsonPath("$.dev", is("Nina Reis")))
                .andExpect(jsonPath("$.bootcamp", is("Testes automatizados")));
        mvc.perform(get("/api/certificados/JRN-D1-B3")).andExpect(status().isOk());
    }

    @Test
    void mentoriaDeOutroDiaBloqueiaOProgresso() throws Exception {
        // Camila está no Spring (curso); Marina ainda não chegou na mentoria futura. Faz a Camila avançar até ela.
        postar("/api/devs/d1/matriculas/b1/progresso", "{}").andExpect(status().isOk());
        postar("/api/devs/d1/matriculas/b1/progresso", "{}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("A mentoria só acontece em 14/10/2026.")));
        mvc.perform(get("/api/devs/d1")).andExpect(jsonPath("$.matriculas[1].podeProgredir", is(false)))
                .andExpect(jsonPath("$.matriculas[1].impedimento", is("A mentoria só acontece em 14/10/2026.")));
    }

    // ---------- erros ----------

    @Test
    void naoEncontradoDa404() throws Exception {
        mvc.perform(get("/api/bootcamps/b99")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", is("Bootcamp não encontrado: b99")));
        mvc.perform(get("/api/devs/d99")).andExpect(status().isNotFound());
        mvc.perform(get("/api/certificados/XYZ")).andExpect(status().isNotFound());
    }

    @Test
    void regraVioladaDa422() throws Exception {
        postar("/api/bootcamps", """
                {"nome":"Sem vagas","dataInicial":"2026-10-02","duracaoEmDias":30,"vagas":0}""")
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.detail", containsString("vagas")));
        postar("/api/bootcamps/b2/conteudos", """
                {"tipo":"DESAFIO","titulo":"Impossível","dificuldade":9}""").andExpect(status().isUnprocessableContent());
    }

    @Test
    void conflitoDa409() throws Exception {
        postar("/api/devs/d1/matriculas", """
                {"bootcampId":"b1"}""").andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Camila Souza já está matriculado em Java Developer.")));
        postar("/api/devs/d4/matriculas", """
                {"bootcampId":"b3"}""").andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("encerrado")));
        postar("/api/bootcamps/b1/conteudos", """
                {"tipo":"CURSO","titulo":"Tarde demais","cargaHoraria":2}""").andExpect(status().isConflict());
        postar("/api/devs", """
                {"nome":"camila souza"}""").andExpect(status().isConflict());
        postar("/api/devs/d4/matriculas/b1/progresso", "{}").andExpect(status().isConflict());
    }

    @Test
    void dadosMalFormadosDao400() throws Exception {
        postar("/api/devs", "{}").andExpect(status().isBadRequest());
        postar("/api/devs", "isto não é json").andExpect(status().isBadRequest());
        postar("/api/bootcamps/b2/conteudos", """
                {"tipo":"VIDEO","titulo":"x"}""").andExpect(status().isBadRequest());
    }

    // ---------- site e segurança ----------

    @Test
    void servePaginaInicialComCabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Jornada</title>")))
                .andExpect(header().string("Content-Security-Policy", startsWith("default-src 'self'")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void healthResponde() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void naoHaNadaInlineNoHtml() throws Exception {
        String html = mvc.perform(get("/index.html")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(html).doesNotContain("onclick=").doesNotContain("style=\"");
        mvc.perform(get("/api/devs/d4")).andExpect(jsonPath("$.matriculas", hasSize(0)))
                .andExpect(jsonPath("$.nivel.nome", is("Iniciante")));
        mvc.perform(get("/api/devs/d4")).andExpect(jsonPath("$.nivel.proximoXp", is(150)));
    }
}
