plugins {
    kotlin("jvm") version "1.9.24"
    java
    id("org.openjfx.javafxplugin") version "0.1.0"

    application
}
dependencies {
    implementation("net.sourceforge.plantuml:plantuml:1.2023.9")
    implementation("org.openjfx:javafx-swing:21")  // required for SwingFXUtils
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
    version = "21.0.1"
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
