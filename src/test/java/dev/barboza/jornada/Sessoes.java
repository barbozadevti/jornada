package dev.barboza.jornada;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** Faz login de verdade pela API e devolve a sessão, como o navegador faria. */
final class Sessoes {

    static final String SENHA = "Jornada@2026";

    private Sessoes() {
    }

    static MockHttpSession entrar(MockMvc mvc, String email) throws Exception {
        var resultado = mvc.perform(post("/api/auth/entrar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
