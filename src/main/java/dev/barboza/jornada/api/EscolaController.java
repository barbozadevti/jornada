package dev.barboza.jornada.api;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.barboza.jornada.aplicacao.Escola;
import dev.barboza.jornada.aplicacao.Escola.NovoConteudo;
import dev.barboza.jornada.api.Pedidos.NovaMatricula;
import dev.barboza.jornada.api.Pedidos.NovoBootcamp;
import dev.barboza.jornada.api.Pedidos.NovoDev;
import dev.barboza.jornada.api.Respostas.BootcampDto;
import dev.barboza.jornada.api.Respostas.CertificadoDto;
import dev.barboza.jornada.api.Respostas.ConteudoDto;
import dev.barboza.jornada.api.Respostas.DevDto;
import dev.barboza.jornada.api.Respostas.ProgressoDto;
import dev.barboza.jornada.api.Respostas.RankingDto;
import dev.barboza.jornada.api.Respostas.RegraXpDto;
import dev.barboza.jornada.dominio.Curso;
import dev.barboza.jornada.dominio.Desafio;
import dev.barboza.jornada.dominio.Mentoria;
import dev.barboza.jornada.dominio.NaoEncontradoException;
import dev.barboza.jornada.dominio.TipoConteudo;
import dev.barboza.jornada.seguranca.UsuarioLogado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Escola", description = "Bootcamps, conteúdos, devs, matrículas, progresso, ranking e certificados.")
public class EscolaController {

    private final Escola escola;

    public EscolaController(Escola escola) {
        this.escola = escola;
    }

    // ---------- bootcamps ----------

    @GetMapping("/bootcamps")
    @Operation(summary = "Lista os bootcamps")
    public List<BootcampDto> bootcamps() {
        return escola.bootcamps().stream().map(b -> BootcampDto.de(b, escola.hoje())).toList();
    }

    @GetMapping("/bootcamps/{id}")
    @Operation(summary = "Mostra um bootcamp com a sua trilha")
    public BootcampDto bootcamp(@PathVariable String id) {
        return BootcampDto.de(escola.bootcamp(id), escola.hoje());
    }

    @PostMapping("/bootcamps")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um bootcamp, ainda sem conteúdos")
    public BootcampDto criarBootcamp(@Valid @RequestBody NovoBootcamp pedido) {
        return BootcampDto.de(escola.criarBootcamp(pedido.nome(), pedido.descricao(), pedido.dataInicial(),
                pedido.duracaoEmDias(), pedido.vagas()), escola.hoje());
    }

    @PostMapping("/bootcamps/{id}/conteudos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Acrescenta um curso, uma mentoria ou um desafio ao fim da trilha",
            description = "Só vale enquanto ninguém estiver matriculado. Curso usa cargaHoraria, mentoria usa data e desafio usa dificuldade.")
    public ConteudoDto adicionarConteudo(@PathVariable String id, @Valid @RequestBody Pedidos.NovoConteudo pedido) {
        return ConteudoDto.de(escola.adicionarConteudo(id, new NovoConteudo(pedido.tipo(), pedido.titulo(),
                pedido.descricao(), pedido.cargaHoraria(), pedido.data(), pedido.dificuldade())));
    }

    // ---------- devs ----------

    @GetMapping("/devs")
    @Operation(summary = "Lista os devs", description = "O coordenador vê todos; o aluno vê só a si mesmo.")
    public List<DevDto> devs(@AuthenticationPrincipal UsuarioLogado usuario) {
        return escola.devs().stream()
                .filter(d -> usuario.coordenador() || d.getId().equals(usuario.devId()))
                .map(d -> DevDto.de(d, escola.hoje())).toList();
    }

    @GetMapping("/devs/{id}")
    @Operation(summary = "Mostra um dev com as suas matrículas e trilhas")
    public DevDto dev(@PathVariable String id, @AuthenticationPrincipal UsuarioLogado usuario) {
        usuario.exigirProprio(id);
        return DevDto.de(escola.dev(id), escola.hoje());
    }

    @PostMapping("/devs")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um dev e a conta de acesso dele", description = "Só o coordenador.")
    public DevDto criarDev(@Valid @RequestBody NovoDev pedido) {
        return DevDto.de(escola.criarDev(pedido.nome(), pedido.email(), pedido.senha()), escola.hoje());
    }

    @PostMapping("/devs/{id}/matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Matricula o dev em um bootcamp")
    public DevDto matricular(@PathVariable String id, @Valid @RequestBody NovaMatricula pedido,
                             @AuthenticationPrincipal UsuarioLogado usuario) {
        usuario.exigirProprio(id);
        escola.matricular(id, pedido.bootcampId());
        return DevDto.de(escola.dev(id), escola.hoje());
    }

    @PostMapping("/devs/{id}/matriculas/{bootcampId}/progresso")
    @Operation(summary = "Conclui o próximo conteúdo da trilha e soma o XP",
            description = "Recusa se a mentoria ainda não aconteceu, se o bootcamp não começou ou já terminou.")
    public ProgressoDto progredir(@PathVariable String id, @PathVariable String bootcampId,
                                  @AuthenticationPrincipal UsuarioLogado usuario) {
        usuario.exigirProprio(id);
        var conclusao = escola.progredir(id, bootcampId);
        return new ProgressoDto(ConteudoDto.de(conclusao.conteudo()), conclusao.conteudo().calcularXp(),
                conclusao.certificado().isPresent(), DevDto.de(escola.dev(id), escola.hoje()));
    }

    // ---------- ranking, certificados e regras ----------

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de XP")
    public List<RankingDto> ranking() {
        var ordenados = escola.ranking();
        return IntStream.range(0, ordenados.size()).mapToObj(i -> {
            var d = ordenados.get(i);
            return new RankingDto(i + 1, d.getId(), d.getNome(), d.calcularTotalXp(), d.nivel().rotulo(), d.certificados().size());
        }).toList();
    }

    @GetMapping("/certificados/{codigo}")
    @Operation(summary = "Confere a autenticidade de um certificado pelo código")
    public CertificadoDto certificado(@PathVariable String codigo) {
        return escola.verificarCertificado(codigo).map(CertificadoDto::de)
                .orElseThrow(() -> new NaoEncontradoException("Nenhum certificado com o código " + codigo + "."));
    }

    @GetMapping("/regras-xp")
    @Operation(summary = "Como cada tipo de conteúdo calcula o XP")
    public List<RegraXpDto> regrasXp() {
        var curso = new Curso("", "Exemplo", "", 8);
        var mentoria = new Mentoria("", "Exemplo", "", escola.hoje());
        var desafio = new Desafio("", "Exemplo", "", 2);
        return List.of(
                regra(TipoConteudo.CURSO, "10 XP por hora de carga horária", curso.explicarXp(), curso.calcularXp()),
                regra(TipoConteudo.MENTORIA, "10 XP + 20 XP, e só conclui a partir do dia marcado", mentoria.explicarXp(), mentoria.calcularXp()),
                regra(TipoConteudo.DESAFIO, "10 XP + 15 XP por nível de dificuldade (1 a 3)", desafio.explicarXp(), desafio.calcularXp()));
    }

    private static RegraXpDto regra(TipoConteudo tipo, String regra, String conta, int xp) {
        return new RegraXpDto(tipo.name(), tipo.rotulo(), regra, conta + " = " + xp + " XP");
    }
}
