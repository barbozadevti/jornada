package dev.barboza.jornada;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import dev.barboza.jornada.dominio.Conteudo;

/**
 * "O diagrama não mente": lê docs/uml/jornada.mmd e confere, por reflexão, que cada classe, atributo,
 * método e relação desenhados existem no código do domínio. Se alguém mudar o código e esquecer
 * o diagrama (ou o contrário), o build quebra.
 */
class DiagramaUmlTest {

    private static final Path DIAGRAMA = Path.of("docs/uml/jornada.mmd");
    private static final String PACOTE = "dev.barboza.jornada.dominio.";

    private static final java.util.regex.Pattern CLASSE = java.util.regex.Pattern.compile("^\\s*class (\\w+)\\s*\\{\\s*$");
    private static final java.util.regex.Pattern ESTEREOTIPO = java.util.regex.Pattern.compile("^\\s*<<(\\w+)>>\\s*$");
    private static final java.util.regex.Pattern METODO = java.util.regex.Pattern.compile("^\\s*[+\\-#~](\\w+)\\((.*?)\\)(\\$)?\\s*(\\S+)?\\s*$");
    private static final java.util.regex.Pattern ATRIBUTO = java.util.regex.Pattern.compile("^\\s*[+\\-#~](\\w+):\\s*(\\S+)\\s*$");
    private static final java.util.regex.Pattern RELACAO = java.util.regex.Pattern.compile("^\\s*(\\w+)\\s*(<\\|--|o--|\\.\\.>)\\s*(\\w+)");

    record Membro(String nome, int parametros, boolean estatico) { }

    record Relacao(String de, String tipo, String para) { }

    private static final Map<String, List<Membro>> metodos = new LinkedHashMap<>();
    private static final Map<String, List<String>> atributos = new LinkedHashMap<>();
    private static final Map<String, String> estereotipos = new LinkedHashMap<>();
    private static final List<Relacao> relacoes = new ArrayList<>();

    @BeforeAll
    static void lerDiagrama() throws IOException {
        String classeAberta = null;
        for (String linha : Files.readAllLines(DIAGRAMA)) {
            java.util.regex.Matcher m;
            if ((m = CLASSE.matcher(linha)).matches()) {
                classeAberta = m.group(1);
                metodos.put(classeAberta, new ArrayList<>());
                atributos.put(classeAberta, new ArrayList<>());
            } else if (linha.trim().equals("}")) {
                classeAberta = null;
            } else if (classeAberta != null && (m = ESTEREOTIPO.matcher(linha)).matches()) {
                estereotipos.put(classeAberta, m.group(1));
            } else if (classeAberta != null && (m = METODO.matcher(linha)).matches()) {
                int parametros = m.group(2).isBlank() ? 0 : m.group(2).split(",").length;
                metodos.get(classeAberta).add(new Membro(m.group(1), parametros, m.group(3) != null));
            } else if (classeAberta != null && (m = ATRIBUTO.matcher(linha)).matches()) {
                atributos.get(classeAberta).add(m.group(1));
            } else if ((m = RELACAO.matcher(linha)).find()) {
                relacoes.add(new Relacao(m.group(1), m.group(2), m.group(3)));
            }
        }
    }

    private static Class<?> exigir(String nome) {
        try {
            return Class.forName(PACOTE + nome);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Classe do diagrama não existe no código: " + nome);
        }
    }

    @Test
    void diagramaFoiLidoPorInteiro() {
        assertThat(metodos).hasSize(11);
        assertThat(relacoes).hasSize(12);
    }

    @TestFactory
    List<DynamicTest> cadaClasseDoDiagramaExisteComSeusMembros() {
        return metodos.keySet().stream().map(nome -> DynamicTest.dynamicTest(nome, () -> {
            Class<?> tipo = exigir(nome);
            for (Membro membro : metodos.get(nome)) {
                boolean existe = Arrays.stream(tipo.getMethods()).anyMatch(metodo ->
                        metodo.getName().equals(membro.nome())
                                && metodo.getParameterCount() == membro.parametros()
                                && Modifier.isStatic(metodo.getModifiers()) == membro.estatico());
                assertThat(existe).as("%s.%s com %d parâmetro(s)", nome, membro.nome(), membro.parametros()).isTrue();
            }
            for (String atributo : atributos.get(nome)) {
                assertThat(Arrays.stream(tipo.getDeclaredFields()).map(Field::getName))
                        .as("atributo %s.%s", nome, atributo).contains(atributo);
            }
            switch (estereotipos.getOrDefault(nome, "")) {
                case "abstract" -> assertThat(Modifier.isAbstract(tipo.getModifiers())).as("%s é abstrata", nome).isTrue();
                case "record" -> assertThat(tipo.isRecord()).as("%s é record", nome).isTrue();
                case "enumeration" -> assertThat(tipo.isEnum()).as("%s é enum", nome).isTrue();
                default -> assertThat(Modifier.isAbstract(tipo.getModifiers()) || tipo.isEnum() || tipo.isRecord())
                        .as("%s deveria ter estereótipo no diagrama", nome).isFalse();
            }
        })).toList();
    }

    @TestFactory
    List<DynamicTest> cadaRelacaoDoDiagramaExisteNoCodigo() {
        return relacoes.stream().map(r -> DynamicTest.dynamicTest(r.de() + " " + r.tipo() + " " + r.para(), () -> {
            Class<?> de = exigir(r.de());
            Class<?> para = exigir(r.para());
            switch (r.tipo()) {
                case "<|--" -> assertThat(de.isAssignableFrom(para) && de != para)
                        .as("%s herda de %s", r.para(), r.de()).isTrue();
                case "o--" -> assertThat(Arrays.stream(de.getDeclaredFields()).anyMatch(campo ->
                        campo.getType() == para || campo.getGenericType().getTypeName().contains(para.getName())))
                        .as("%s guarda %s", r.de(), r.para()).isTrue();
                case "..>" -> assertThat(usa(de, para)).as("%s usa %s", r.de(), r.para()).isTrue();
                default -> throw new AssertionError("Relação desconhecida: " + r.tipo());
            }
        })).toList();
    }

    /** "Usa" = aparece em algum campo, parâmetro ou retorno (inclusive como argumento de tipo genérico). */
    private static boolean usa(Class<?> de, Class<?> para) {
        String nome = para.getName();
        return Arrays.stream(de.getDeclaredFields()).anyMatch(f -> f.getGenericType().getTypeName().contains(nome))
                || Arrays.stream(de.getDeclaredMethods()).anyMatch(m ->
                m.getGenericReturnType().getTypeName().contains(nome)
                        || Arrays.stream(m.getGenericParameterTypes()).anyMatch(p -> p.getTypeName().contains(nome)));
    }

    @Test
    void todoTipoDeConteudoDoCodigoEstaNoDiagrama() {
        List<String> noCodigo = Arrays.stream(Conteudo.class.getPermittedSubclasses()).map(Class::getSimpleName).toList();
        List<String> noDiagrama = relacoes.stream().filter(r -> r.de().equals("Conteudo") && r.tipo().equals("<|--"))
                .map(Relacao::para).toList();
        assertThat(noDiagrama).containsExactlyInAnyOrderElementsOf(noCodigo);
    }
}
