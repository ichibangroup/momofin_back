val persistenceVersion = "3.1.0"
val postgresVersion = "42.6.0"
val jsonWebTokenVersion = "0.9.1"
val javaxVersion = "2.3.1"
val gcpVersion = "2.43.1"
val sentryVersion = "6.10.0"

plugins {
    java
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
    id("io.sentry.jvm.gradle") version "4.12.0"
}

group = "ppl.momofin"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("jakarta.persistence:jakarta.persistence-api:$persistenceVersion")
    implementation("io.jsonwebtoken:jjwt:$jsonWebTokenVersion")
    implementation("javax.xml.bind:jaxb-api:$javaxVersion")
    implementation("com.google.cloud:google-cloud-storage:$gcpVersion")
    implementation("io.sentry:sentry:$sentryVersion")
    implementation("io.sentry:sentry-spring-boot-starter:$sentryVersion")
    implementation ("org.springframework.boot:spring-boot-starter-hateoas")


    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("org.postgresql:postgresql:$postgresVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.test{
    filter{
        excludeTestsMatching("*FunctionalTest")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport{
    reports {
        xml.required = true
    }
    dependsOn(tasks.test)
}

sonar {
    properties {
        property("sonar.projectKey", "ichibangroup_momofin_back")
        property("sonar.organization", "ichibangroup")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

sentry {
    includeSourceContext = true

    org= "muhamad-pascal-alfin-pahlevi"
    projectName = "momofin-backend"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}
