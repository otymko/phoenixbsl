import java.net.URI

plugins {
    java
    maven
    jacoco
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.sonarqube") version "2.8"
    id("io.franzbecker.gradle-lombok") version "3.2.0"
}

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

group = "org.github.otymko.phoenixbsl"
version = "0.3.3"

dependencies {
    testImplementation("com.hynnet", "jacob", "1.18")
    testImplementation("junit", "junit", "4.12")
    implementation("net.java.dev.jna:jna-platform:5.4.0")
    implementation("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.8.1")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("com.jfoenix","jfoenix", "9.0.9")
    implementation("lc.kra.system","system-hook", "3.5")

    compileOnly("org.projectlombok", "lombok", lombok.version)
}

configure<JavaPluginConvention> {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

// TODO: путь к jar после build, можно сделать лучше?
var jarName = ""

tasks.jar {
    jarName = this.archiveFileName.get()
    manifest {
        attributes["Main-Class"] = "org.github.otymko.phoenixbsl.LauncherApp"
        attributes["Implementation-Version"] = project.version
    }
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    project.configurations.implementation.get().isCanBeResolved = true
    configurations = listOf(project.configurations["implementation"])
    archiveClassifier.set("")
}

tasks.register<Exec>("jpackage") {
    var jpackage = System.getenv("JPACKAGE_HOME") + "/jpackage.exe"
    executable(jpackage)
    args(
            "--name", "phoenixbsl",
            "--type", "msi",
            "--input", "build/libs",
            "--main-jar", jarName,
            "--win-dir-chooser",
            "--win-shortcut",
            "--win-menu",
            "--app-version", project.version,
            "--vendor", "otymko"
    )
}

sonarqube {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "phoenixbsl")
        property("sonar.projectKey", "phoenixbsl")
        property("sonar.projectName", "Phoenix BSL")
        property("sonar.exclusions", "**/gen/**/*.*")
    }
}

javafx {
    version = "13"
    modules("javafx.controls", "javafx.fxml")
}

lombok {
    version = "1.18.10"
    sha256 = "2836e954823bfcbad45e78c18896e3d01058e6f643749810c608b7005ee7b2fa"
}
