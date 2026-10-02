package dev.barboza.jornada.api;

import java.time.LocalDate;
import java.util.List;

import dev.barboza.jornada.dominio.Bootcamp;
import dev.barboza.jornada.dominio.Certificado;
import dev.barboza.jornada.dominio.Conteudo;
import dev.barboza.jornada.dominio.Dev;
import dev.barboza.jornada.dominio.Matricula;
import dev.barboza.jornada.dominio.NivelDev;

/** O formato das respostas da API: o domínio é traduzido aqui, sem expor as classes de negócio. */
public final class Respostas {

    private Respostas() {
    }

    public record ConteudoDto(String id, String tipo, String tipoRotulo, String titulo, String descricao,
                              String detalhe, int xp, String explicacaoXp) {
        static ConteudoDto de(Conteudo c) {
            return new ConteudoDto(c.getId(), c.tipo().name(), c.tipo().rotulo(), c.getTitulo(), c.getDescricao(),
                    c.detalhe(), c.calcularXp(), c.explicarXp());
        }
    }

    public record BootcampDto(String id, String nome, String descricao, LocalDate dataInicial, LocalDate dataFinal,
                              String situacao, String situacaoRotulo, int vagas, int matriculados, int vagasRestantes,
                              int xpTotal, boolean trilhaCongelada, List<ConteudoDto> conteudos) {
        static BootcampDto de(Bootcamp b, LocalDate hoje) {
            var situacao = b.situacao(hoje);
            return new BootcampDto(b.getId(), b.getNome(), b.getDescricao(), b.getDataInicial(), b.getDataFinal(),
                    situacao.name(), situacao.rotulo(), b.getVagas(), b.getMatriculas().size(),
                    b.getVagas() - b.getMatriculas().size(), b.calcularXpTotal(), !b.getMatriculas().isEmpty(),
                    b.getConteudos().stream().map(ConteudoDto::de).toList());
        }
    }

    public record PassoDto(ConteudoDto conteudo, String estado, LocalDate concluidoEm) {
    }

    public record CertificadoDto(String codigo, String dev, String bootcamp, LocalDate emitidoEm, int xp) {
        static CertificadoDto de(Certificado c) {
            return new CertificadoDto(c.codigo(), c.dev(), c.bootcamp(), c.emitidoEm(), c.xp());
        }
    }

    public record MatriculaDto(String bootcampId, String bootcampNome, String situacaoBootcamp, LocalDate inscritaEm,
                               int concluidos, int total, int percentual, int xpGanho, int xpTotal,
                               boolean podeProgredir, String impedimento, ConteudoDto proximo, List<PassoDto> trilha,
                               CertificadoDto certificado) {
        static MatriculaDto de(Matricula m, LocalDate hoje) {
            var conteudos = m.getBootcamp().getConteudos();
            var proximo = m.proximo();
            var impedimento = m.impedimento(hoje);
            List<PassoDto> trilha = conteudos.stream().map(c -> {
                LocalDate em = m.getConcluidos().get(c);
                String estado = em != null ? "CONCLUIDO" : proximo.filter(c::equals).isPresent() ? "PROXIMO" : "PENDENTE";
                return new PassoDto(ConteudoDto.de(c), estado, em);
            }).toList();
            return new MatriculaDto(m.getBootcamp().getId(), m.getBootcamp().getNome(),
                    m.getBootcamp().situacao(hoje).name(), m.getInscritaEm(), m.getConcluidos().size(), conteudos.size(),
                    m.percentual(), m.xpGanho(), m.getBootcamp().calcularXpTotal(),
                    proximo.isPresent() && impedimento.isEmpty(), impedimento.orElse(null),
                    proximo.map(ConteudoDto::de).orElse(null), trilha, m.certificado().map(CertificadoDto::de).orElse(null));
        }
    }

    public record NivelDto(String codigo, String nome, int xpMinimo, String proximoNome, Integer proximoXp, int percentual) {
        static NivelDto de(int xp) {
            NivelDev nivel = NivelDev.paraXp(xp);
            var proximo = nivel.proximo();
            int percentual = proximo.map(p -> (xp - nivel.xpMinimo()) * 100 / (p.xpMinimo() - nivel.xpMinimo())).orElse(100);
            return new NivelDto(nivel.name(), nivel.rotulo(), nivel.xpMinimo(), proximo.map(NivelDev::rotulo).orElse(null),
                    proximo.map(NivelDev::xpMinimo).orElse(null), percentual);
        }
    }

    public record DevDto(String id, String nome, int xp, NivelDto nivel, int certificados, List<MatriculaDto> matriculas) {
        static DevDto de(Dev d, LocalDate hoje) {
            int xp = d.calcularTotalXp();
            return new DevDto(d.getId(), d.getNome(), xp, NivelDto.de(xp), d.certificados().size(),
                    d.getMatriculas().stream().map(m -> MatriculaDto.de(m, hoje)).toList());
        }
    }

    public record RankingDto(int posicao, String id, String nome, int xp, String nivel, int certificados) {
    }

    public record ProgressoDto(ConteudoDto concluido, int xpGanho, boolean certificadoEmitido, DevDto dev) {
    }

    public record RegraXpDto(String tipo, String tipoRotulo, String regra, String exemplo) {
    }
}
