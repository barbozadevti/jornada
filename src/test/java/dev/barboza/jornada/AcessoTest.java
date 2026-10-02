package dev.barboza.jornada;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Quem pode o quê: login, sessão, CSRF, bloqueio por senhas erradas e o aluno vendo só a própria jornada. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiTest.Relogio.class)
class AcessoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RelogioAjustavel relogio;

    MockHttpSession coord;

    @BeforeEach
    void dadosDeExemplo() throws Exception {
        coord = Sessoes.entrar(mvc, "coordenador@jornada.dev");
        mvc.perform(post("/api/demo/reiniciar").session(coord).with(csrf())).andExpect(status().isNoContent());
    }

    private ResultActions entrar(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/entrar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"));
    }

    private ResultActions postar(MockHttpSession sessao, String rota, String json) throws Exception {
        return mvc.perform(post(rota).session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    // ---------- login ----------

    @Test
    void semLoginAApiRecusa() throws Exception {
        mvc.perform(get("/api/bootcamps")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("Faça login para continuar.")));
        mvc.perform(get("/api/ranking")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
    }

    @Test
    void certificadoEPublicoParaQuemRecebeOCodigo() throws Exception {
        mvc.perform(get("/api/certificados/JRN-D1-B3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.dev", is("Camila Souza")));
    }

    @Test
    void entrarDevolveQuemEEOPapel() throws Exception {
        entrar("Camila@Jornada.dev", Sessoes.SENHA).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Camila Souza")))
                .andExpect(jsonPath("$.papel", is("ALUNO")))
                .andExpect(jsonPath("$.devId", is("d1")));
        var sessao = Sessoes.entrar(mvc, "coordenador@jornada.dev");
        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(jsonPath("$.papel", is("COORDENADOR")));
    }

    @Test
    void emailInexistenteESenhaErradaDaoAMesmaMensagem() throws Exception {
        entrar("camila@jornada.dev", "errada-errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("E-mail ou senha incorretos.")));
        entrar("ninguem@jornada.dev", "errada-errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("E-mail ou senha incorretos.")));
    }

    @Test
    void cincoSenhasErradasBloqueiamPorDezMinutos() throws Exception {
        for (int i = 0; i < 5; i++) {
            entrar("marina@jornada.dev", "errada-" + i).andExpect(status().isUnauthorized());
        }
        entrar("marina@jornada.dev", Sessoes.SENHA).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("Muitas tentativas. Aguarde 10 minutos e tente de novo.")));
        relogio.avancarMinutos(11);
        entrar("marina@jornada.dev", Sessoes.SENHA).andExpect(status().isOk());
    }

    @Test
    void sairEncerraASessao() throws Exception {
        var sessao = Sessoes.entrar(mvc, "joao@jornada.dev");
        mvc.perform(post("/api/auth/sair").session(sessao).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/bootcamps").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void postSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/api/auth/entrar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"camila@jornada.dev\",\"senha\":\"" + Sessoes.SENHA + "\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/devs/d1/matriculas").session(coord).contentType(MediaType.APPLICATION_JSON)
                .content("{\"bootcampId\":\"b2\"}")).andExpect(status().isForbidden());
    }

    // ---------- o aluno só vê e mexe no que é dele ----------

    @Test
    void alunoVeSoASiMesmoNaListaEnoDetalhe() throws Exception {
        var joao = Sessoes.entrar(mvc, "joao@jornada.dev");
        mvc.perform(get("/api/devs").session(joao)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].nome", is("João Pereira")));
        mvc.perform(get("/api/devs/d2").session(joao)).andExpect(status().isOk());
        mvc.perform(get("/api/devs/d1").session(joao)).andExpect(status().isNotFound());
    }

    @Test
    void alunoNaoMexeNaJornadaDeOutro() throws Exception {
        var joao = Sessoes.entrar(mvc, "joao@jornada.dev");
        postar(joao, "/api/devs/d1/matriculas/b1/progresso", "{}").andExpect(status().isNotFound());
        postar(joao, "/api/devs/d1/matriculas", "{\"bootcampId\":\"b2\"}").andExpect(status().isNotFound());
    }

    @Test
    void alunoProgrideEMatriculaASiMesmo() throws Exception {
        var joao = Sessoes.entrar(mvc, "joao@jornada.dev");
        postar(joao, "/api/devs/d2/matriculas/b1/progresso", "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.xpGanho", is(30)));
        postar(joao, "/api/devs/d2/matriculas", "{\"bootcampId\":\"b2\"}").andExpect(status().isCreated());
    }

    @Test
    void alunoEnxergaOCatalogoEORanking() throws Exception {
        var joao = Sessoes.entrar(mvc, "joao@jornada.dev");
        mvc.perform(get("/api/bootcamps").session(joao)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/ranking").session(joao)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void alunoNaoAdministra() throws Exception {
        var joao = Sessoes.entrar(mvc, "joao@jornada.dev");
        postar(joao, "/api/bootcamps", "{\"nome\":\"X\",\"dataInicial\":\"2026-10-02\",\"duracaoEmDias\":30,\"vagas\":5}")
                .andExpect(status().isForbidden());
        postar(joao, "/api/bootcamps/b1/conteudos", "{\"tipo\":\"CURSO\",\"titulo\":\"X\",\"cargaHoraria\":2}")
                .andExpect(status().isForbidden());
        postar(joao, "/api/devs", "{\"nome\":\"Intruso\",\"email\":\"i@teste.dev\",\"senha\":\"senha-forte-1\"}")
                .andExpect(status().isForbidden());
        postar(joao, "/api/demo/reiniciar", "{}").andExpect(status().isForbidden());
    }

    @Test
    void devNovoJaEntraComASenhaQueOCoordenadorDefiniu() throws Exception {
        postar(coord, "/api/devs", "{\"nome\":\"Nina Reis\",\"email\":\"nina@teste.dev\",\"senha\":\"senha-forte-1\"}")
                .andExpect(status().isCreated());
        entrar("nina@teste.dev", "senha-forte-1").andExpect(status().isOk()).andExpect(jsonPath("$.devId", is("d5")));
    }
}
