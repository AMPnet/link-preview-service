plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.40")
    application
}

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
    compile("ch.qos.logback:logback-classic:1.2.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application
    mainClassName = "com.ampnet.AppKt"
}
