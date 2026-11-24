# Estágio 1: compila o projeto com o Maven Wrapper (dependências em cache separado)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sh ./mvnw -B -q dependency:go-offline

COPY src src
RUN sh ./mvnw -B -q package -DskipTests

# Estágio 2: imagem de execução enxuta, com usuário sem privilégios
FROM eclipse-temurin:21-jre
RUN groupadd --system thindesk && useradd --system --gid thindesk thindesk
WORKDIR /app

COPY --from=build /build/target/thindesk-*.jar app.jar

USER thindesk
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
