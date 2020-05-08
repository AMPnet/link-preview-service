import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.72")
    id("com.google.cloud.tools.jib") version "2.1.0"
    application
}

group = "com.ampnet"
version = "0.0.7"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val ktorVersion = "1.3.2"
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("net.sourceforge.htmlcleaner:htmlcleaner:2.23")
    implementation("org.apache.commons:commons-text:1.8")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val main_class = "com.ampnet.AppKt"

application {
    // Define the main class for the application
    mainClassName = main_class
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

jib {
    val dockerUsername: String = System.getenv("DOCKER_USERNAME") ?: "DOCKER_USERNAME"
    val dockerPassword: String = System.getenv("DOCKER_PASSWORD") ?: "DOCKER_PASSWORD"

    to {
        image = "ampnet/link-preview-service:$version"
        auth {
            username = dockerUsername
            password = dockerPassword
        }
        tags = setOf("latest")
    }
    container {
        mainClass = main_class
        creationTime = "USE_CURRENT_TIMESTAMP"

        // good defaults intended for Java 8 (>= 8u191) containers
        jvmFlags = listOf(
                "-server",
                "-Djava.awt.headless=true",
                "-Dfile.encoding=UTF8",
                "-XX:InitialRAMFraction=2",
                "-XX:MinRAMFraction=2",
                "-XX:MaxRAMFraction=2",
                "-XX:MaxGCPauseMillis=100",
                "-XX:+UseStringDeduplication"
        )
    }
}
