package dev.barboza.jornada.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.barboza.jornada.aplicacao.Escola;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demonstração")
public class DemoController {

    private final Escola escola;

    public DemoController(Escola escola) {
        this.escola = escola;
    }

    @PostMapping("/reiniciar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga tudo e volta aos dados de exemplo")
    public void reiniciar() {
        escola.reiniciar();
    }
}
