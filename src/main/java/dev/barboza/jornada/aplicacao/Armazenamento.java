package dev.barboza.jornada.aplicacao;

import java.util.Optional;

/** Onde o estado da escola é guardado entre uma execução e outra. */
public interface Armazenamento {

    Optional<Estado> carregar();

    void salvar(Estado estado);

    /** Não guarda nada: o estado vale só enquanto o servidor estiver de pé (usado nos testes). */
    static Armazenamento emMemoria() {
        return new Armazenamento() {
            @Override
            public Optional<Estado> carregar() {
                return Optional.empty();
            }

            @Override
            public void salvar(Estado estado) {
                // nada a fazer
            }
        };
    }
}
