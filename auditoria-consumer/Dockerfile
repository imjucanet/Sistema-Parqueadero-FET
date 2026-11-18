# Etapa 1: compilación del artefacto
FROM gradle:8.14.2-jdk21 AS build
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./
COPY src src

RUN gradle bootJar --no-daemon

# Etapa 2: imagen de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/auditoria-consumer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
