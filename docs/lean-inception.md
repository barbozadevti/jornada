# Lean Inception: Jornada

> Decisões de produto no formato Lean Inception (Paulo Caroli):
> visão → escopo → personas → jornadas → funcionalidades → sequenciamento → MVP.

---

## 1. Visão do produto

**Para** quem faz, organiza ou avalia bootcamps de programação (alunos, coordenadores e recrutadores técnicos)
**cujo** problema é que o progresso em um bootcamp fica espalhado em planilhas e plataformas que não mostram o caminho inteiro nem o que falta para concluir,
**a Jornada** é uma plataforma de bootcamps
**que** organiza cada bootcamp como uma trilha de cursos, mentorias e desafios, soma XP a cada conteúdo concluído e emite um certificado verificável no fim.
**Diferente de** um quadro de tarefas genérico,
**o nosso produto** tem regras próprias de bootcamp (ordem da trilha, mentoria com data, vagas, período) e um domínio em Java que demonstra os quatro pilares da orientação a objetos.

## 2. O produto É / NÃO É / FAZ / NÃO FAZ

| É | NÃO É |
|---|---|
| Um modelo OO de bootcamp, executável | Um LMS completo, com vídeo e provas |
| Uma vitrine de abstração, herança, polimorfismo e encapsulamento | Um sistema de pagamento ou de matrícula paga |
| Um acompanhamento de trilha com XP, nível e certificado | Um avaliador automático de código |

| FAZ | NÃO FAZ |
|---|---|
| Cadastra bootcamps e monta a trilha com três tipos de conteúdo | Hospeda aulas ou materiais |
| Matricula devs respeitando vagas, período e duplicidade | Exige login (é uma demonstração; sem autenticação) |
| Calcula o XP de cada tipo pela sua própria regra | Compara com outras plataformas |
| Bloqueia a mentoria até o dia marcado | Envia e-mails ou notificações |
| Emite certificado com código conferível | Gera PDF (o certificado imprime pelo navegador) |

## 3. Objetivos do produto

1. **Modelo fiel ao desafio:** `Conteudo` abstrata, `Curso`, `Mentoria` e `Desafio` herdando dela, `Bootcamp` e `Dev`, com `calcularXp()` polimórfico.
2. **Regras que valem de verdade:** o que o enunciado deixa solto (ordem, datas, vagas) vira regra testada.
3. **Diagrama que não mente:** o UML fica no repositório e um teste falha se ele divergir do código.
4. **Fácil de avaliar:** abre com um clique, já com dados de exemplo, e a aba "Por dentro" liga cada pilar ao código.

## 4. Personas

### Camila, aluna do bootcamp (27 anos)
- **Comportamento:** estuda à noite, em blocos curtos; larga quando perde o fio.
- **Necessidades:** saber qual é o próximo passo, ver o quanto já andou e sentir que está avançando.

### Roberto, coordenador do bootcamp (39 anos)
- **Comportamento:** monta a turma, define a trilha e acompanha quem está parado.
- **Necessidades:** criar um bootcamp sem planilha, controlar vagas e ver o ranking da turma.

### Marina, recrutadora técnica (34 anos)
- **Comportamento:** avalia dezenas de portfólios por semana, gasta poucos minutos em cada um.
- **Necessidades:** entender rápido o que o projeto faz, ver sinais de boas práticas e conferir um certificado.

### Lucas, desenvolvedor sênior da entrevista técnica (41 anos)
- **Comportamento:** abre o código, procura os testes e as decisões de arquitetura.
- **Necessidades:** domínio limpo, regras testadas e escolhas justificadas.

## 5. Jornadas

**Camila avança na trilha**
1. Abre a Jornada e escolhe o próprio nome em *Devs*.
2. Vê o nível, o XP e a trilha do Java Developer com o próximo conteúdo destacado.
3. Conclui "Spring Boot e APIs REST": ganha 100 XP e sobe de nível.
4. O conteúdo seguinte é uma mentoria marcada para daqui a 12 dias: a tela explica por que está bloqueado.

**Roberto monta um bootcamp**
1. Clica em *Novo bootcamp*, define nome, início, duração e vagas.
2. Acrescenta curso, mentoria e desafio, vendo a regra de XP de cada um.
3. Matricula os devs; a trilha congela.
4. Acompanha o *Ranking*.

**Marina confere um certificado**
1. Recebe o código `JRN-D1-B3` de uma candidata.
2. Digita no campo de conferência do *Ranking* e vê quem concluiu, quando e com quantos XP.

## 6. Funcionalidades e ondas

Avaliadas por **esforço**, **valor de negócio** e **valor de aprendizado/portfólio**.

### Onda 1: o desafio (MVP)
| Funcionalidade | Esforço | Valor |
|---|---|---|
| `Conteudo` abstrata com `Curso`, `Mentoria` e `Desafio` e `calcularXp()` polimórfico | baixo | alto |
| `Bootcamp` com trilha ordenada e `Dev` com XP total | baixo | alto |
| Matrícula e progresso pela trilha | médio | alto |
| API REST + página para ver bootcamps, devs e concluir conteúdos | médio | alto |

### Onda 2: regras de bootcamp
| Funcionalidade | Esforço | Valor |
|---|---|---|
| Vagas, período (próximo, em andamento, encerrado) e matrícula repetida | baixo | alto |
| Mentoria só conclui a partir da sua data | baixo | médio |
| Trilha congelada depois da primeira matrícula | baixo | médio |
| Nível do dev pelo XP e ranking | baixo | médio |

### Onda 3: reconhecimento e persistência
| Funcionalidade | Esforço | Valor |
|---|---|---|
| Certificado ao concluir a trilha, imprimível | médio | alto |
| Conferência pública do certificado pelo código | baixo | médio |
| Estado em arquivo JSON, remontado pelas regras do domínio ao carregar | médio | alto |

### Onda 4: portfólio
| Funcionalidade | Esforço | Valor |
|---|---|---|
| Aba "Por dentro": pilares da OO ligados ao código, regras de XP ao vivo, UML | médio | alto |
| Diagrama UML conferido por teste contra o código | médio | alto |
| CI com testes, teste de fumaça e imagem Docker | baixo | médio |

### Próximas ondas (fora do escopo agora)
- Edição e remoção de conteúdos (hoje a trilha só cresce, e congela com alunos).
- Turmas com várias edições do mesmo bootcamp.
- Pré-requisitos entre bootcamps e conteúdos opcionais.
- Autenticação, com o aluno vendo só a própria jornada.

## 7. Canvas MVP

| | |
|---|---|
| **Proposta de valor** | Trilha clara, XP a cada passo, certificado no fim. |
| **Segmentos** | Alunos, coordenadores, recrutadores e devs que estudam POO. |
| **Resultados esperados** | O avaliador entende o produto em 2 minutos e encontra os quatro pilares no código. |
| **Métricas** | Todos os testes verdes; diagrama sem divergência; zero configuração para rodar. |
| **Custos** | Só o tempo de desenvolvimento; sem serviços pagos. |

## 8. Decisões de arquitetura

- **Domínio sem Spring:** as classes de `dominio` são Java puro; só `aplicacao`, `api` e `config` conhecem o framework.
- **`sealed`:** `Conteudo` permite só `Curso`, `Mentoria` e `Desafio`; o compilador conhece todos os casos.
- **A mentoria decide por si:** `impedimentoParaConcluir(hoje)` é um método polimórfico, sem `instanceof` espalhado.
- **Relógio injetado:** nada chama `LocalDate.now()` no domínio; a data entra por parâmetro, e os testes controlam o tempo.
- **Persistência por remontagem:** o arquivo guarda fatos (quem se matriculou, quando, o que concluiu e em que dia); ao carregar, o domínio é refeito pelos mesmos métodos, então as regras valem também para o que veio do disco.
- **Arquivo gravado por troca atômica:** escreve em um temporário e move para o lugar, nunca deixando o JSON pela metade.
- **Sem banco de dados:** o volume de dados não justifica; a interface `Armazenamento` deixa a troca para depois.
- **Erros em problem+json (RFC 9457):** 404 (não encontrado), 409 (não cabe agora), 422 (regra violada) e 400 (formato).
- **Front-end sem framework:** módulos ES, DOM com `textContent` (sem `innerHTML`) e política de segurança de conteúdo sem nada inline.
