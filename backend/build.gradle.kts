import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.14.0"
}

group = "com.fet"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val openApiGeneratedDir = layout.buildDirectory.dir("generated/openapi")

sourceSets.named("main") {
    java.srcDir(openApiGeneratedDir.map { it.dir("src/main/java") })
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("com.graphql-java:graphql-java-extended-scalars:24.0")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.9.1")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<GenerateTask>().configureEach {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/src/main/resources/api/openapi.yaml")
    outputDir.set(openApiGeneratedDir.map { it.asFile.absolutePath })
    apiPackage.set("com.fet.parqueadero.adapter.in.rest.api")
    invokerPackage.set("com.fet.parqueadero.adapter.in.rest.api")
    modelPackage.set("com.fet.parqueadero.adapter.in.rest.dto")

    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useTags" to "true",
            "dateLibrary" to "java8",
            "documentationProvider" to "springdoc",
            "useJakartaValidation" to "true",
            "openApiNullable" to "false",
            "serializableModel" to "false",
            "hideGenerationTimestamp" to "true"
        )
    )
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Los contratos viven dentro de backend/src/main/resources (son los que carga la
// aplicacion: Spring lee el .graphqls y openapi-generator lee el .yaml). Las copias
// de 01_OpenAPI/ y 02_GraphQL/ son los entregables del taller, y estas tareas las
// mantienen iguales en vez de sincronizarlas a mano. Se omiten cuando esas carpetas
// no existen, por ejemplo dentro del build de Docker, cuyo contexto es solo backend/.
fun registrarSincronizacionDeContrato(nombre: String, origenRelativo: String, destinoRelativo: String) =
    tasks.register(nombre) {
        description = "Copia $origenRelativo sobre la copia entregable $destinoRelativo."
        group = "documentation"
        val origen = layout.projectDirectory.file(origenRelativo).asFile
        val destino = layout.projectDirectory.file(destinoRelativo).asFile
        inputs.file(origen)
        onlyIf { destino.parentFile.isDirectory }
        outputs.upToDateWhen { destino.isFile && destino.readText() == origen.readText() }
        doLast {
            destino.writeText(origen.readText())
            logger.lifecycle("Contrato sincronizado en ${destino.path}")
        }
    }

val sincronizarEsquemaGraphQL = registrarSincronizacionDeContrato(
    "sincronizarEsquemaGraphQL",
    "src/main/resources/graphql/schema.graphqls",
    "../02_GraphQL/schema.graphql")

val sincronizarContratoOpenAPI = registrarSincronizacionDeContrato(
    "sincronizarContratoOpenAPI",
    "src/main/resources/api/openapi.yaml",
    "../01_OpenAPI/openapi.yaml")

tasks.named("processResources") {
    finalizedBy(sincronizarEsquemaGraphQL, sincronizarContratoOpenAPI)
}
