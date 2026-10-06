# ─────────────────────────────────────────────────────────
# Dockerfile de RUNTIME
#
# O JAR é compilado FORA do Docker (pelo GitHub Actions, com Gradle +
# cache de dependências) e passado para cá via build-arg JAR_FILE.
# Assim o build da imagem é rápido e leve — não compila nada aqui.
#
# Build local (se precisar):
#   ./gradlew bootJar
#   docker build --build-arg JAR_FILE=build/libs/*.jar -t myfitness-app .
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuário não-root por segurança
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Caminho do JAR compilado — default aponta para a saída padrão do Gradle
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENV SPRING_PROFILE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
