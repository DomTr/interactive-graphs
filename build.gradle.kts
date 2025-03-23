plugins {
    kotlin("jvm") version "1.9.24"
    java
    id("org.openjfx.javafxplugin") version "0.1.0"

    application
}
dependencies {
    implementation("net.sourceforge.plantuml:plantuml:1.2023.9") // Check for the latest version
}

javafx {
    // will pull in transitive modules
    modules("javafx.controls", "javafx.fxml") // replace with what you modules need

    // another option is to use:
    // modules = listOf("javafx.controls", "javafx.fxml")

    version = "21.0.1" // or whatever version you're using
}
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("GraphVisualizerAppKt")
}
