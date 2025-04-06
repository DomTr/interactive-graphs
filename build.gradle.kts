plugins {
    kotlin("jvm") version "1.9.24"
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    kotlin("plugin.serialization") version "1.9.24" // For json. Otherwise, compile plugin will not be active and @Serializable annotation will not work

    application
}
dependencies {
    implementation("net.sourceforge.plantuml:plantuml:1.2023.9")
    implementation("org.openjfx:javafx-swing:21")  // required for SwingFXUtils
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("javax.xml.bind:jaxb-api:2.3.1") // DOM parsing/writing

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // JUnit 5 support
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
