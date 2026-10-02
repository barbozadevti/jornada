# Etapa 1: compila com Maven (dependências em camada própria, para aproveitar o cache).
FROM maven:3.9-eclipse-temurin-21 AS compilacao
WORKDIR /fonte
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: só o JRE e o jar, com usuário sem privilégios.
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 aluno && mkdir /dados && chown aluno /dados
WORKDIR /app
COPY --from=compilacao /fonte/target/jornada.jar jornada.jar
USER aluno
# Bootcamps, devs e progresso ficam em /dados: monte um volume para não perder ao recriar o contêiner.
ENV JORNADA_ARQUIVO=/dados/jornada.json
VOLUME /dados
EXPOSE 5270
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "jornada.jar"]
