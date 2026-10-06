# ─────────────────────────────────────────────────────────
# Stage 1 — Build
# Compila a aplicação com Gradle e gera o JAR executável.
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copia apenas os arquivos de build primeiro — aproveita o cache de camadas
# do Docker: dependências só são rebaixadas quando esses arquivos mudam.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Baixa as dependências (camada cacheada enquanto build.gradle não mudar)
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# Copia o código-fonte e compila o JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ─────────────────────────────────────────────────────────
# Stage 2 — Runtime
# Imagem enxuta só com o JRE para rodar o JAR.
# eclipse-temurin tem imagens multi-arch (funciona em ARM e x86).
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuário não-root por segurança
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copia o JAR executável gerado no stage de build.
# O build.gradle desabilita a task 'jar', então só existe o bootJar aqui.
COPY --from=build /app/build/libs/*.jar app.jar

# Porta da aplicação (context-path /api é interno ao Spring)
EXPOSE 8080

# Perfil de produção por padrão
ENV SPRING_PROFILE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
