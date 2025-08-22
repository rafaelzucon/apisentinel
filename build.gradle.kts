apply(from = "variables.gradle.kts")

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    jacoco
}

group = "br.com.apisentinel"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("io.projectreactor:reactor-test:3.6.9")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    testImplementation("com.google.guava:guava:33.2.1-jre")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

val csvFileName: String by extra
val apisentinelCron: String by extra

val csvInputPath: String =
    (project.findProperty("csvPath") as String?)
        ?: "data/$csvFileName"

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    systemProperty("apisentinel.csv.input", csvInputPath)
    systemProperty("apisentinel.scheduling.cron", apisentinelCron)
}

tasks.withType<Test> {
    systemProperty("apisentinel.csv.input", csvInputPath)
    systemProperty("apisentinel.scheduling.cron", apisentinelCron)
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

//tasks.jacocoTestCoverageVerification {
//    violationRules {
//        rule {
//            element = "BUNDLE"
//            limit {
//                counter = "INSTRUCTION"
//                value = "COVEREDRATIO"
//                minimum = "0.05".toBigDecimal()
//            }
//            limit {
//                counter = "BRANCH"
//                value = "COVEREDRATIO"
//                minimum = "0.05".toBigDecimal()
//            }
//            limit {
//                counter = "LINE"
//                value = "COVEREDRATIO"
//                minimum = "0.05".toBigDecimal()
//            }
//        }
//    }
//}
//
//tasks.check {
//    dependsOn(tasks.jacocoTestCoverageVerification)
//}
