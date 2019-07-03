plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.40")
    id("com.google.cloud.tools.jib") version "1.3.0"
    application
}

group = "com.ampnet"
version = "0.0.2"

repositories {
    jcenter()
}

dependencies {
    val ktorVersion = "1.2.2"
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("net.sourceforge.htmlcleaner:htmlcleaner:2.16")
    implementation("org.apache.commons:commons-text:1.6")
    compile("ch.qos.logback:logback-classic:1.2.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

val main_class = "com.ampnet.AppKt"

application {
    // Define the main class for the application
    mainClassName = main_class
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
        useCurrentTimestamp = true

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
