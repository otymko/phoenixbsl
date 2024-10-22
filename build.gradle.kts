import java.net.URI

import com.github.gradle_git_version_calculator.GitRepository;
import com.github.gradle_git_version_calculator.GitCommandsFactory;
import com.github.gradle_git_version_calculator.GitVersionCalculator;


plugins {
    java
    jacoco
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.sonarqube") version "5.1.0.4882"
    id("io.franzbecker.gradle-lombok") version "4.0.0"
    id("com.github.gradle-git-version-calculator") version "1.1.0"
}

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

group = "com.github.otymko.phoenixbsl"
version = gitVersionCalculator.calculateVersion("v")

val semver = calculateVersion("v", false)
val javaOptionsForJavaFx = "--add-exports javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED " +
        "--add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED " +
        "--add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED " +
        "--add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED"

dependencies {

    implementation("net.java.dev.jna:jna-platform:5.9.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.12.0")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("lc.kra.system:system-hook:3.8")
    implementation("com.github.silverbulleters:sonarlint-core:123a8b2")

    // ui
    implementation("com.jfoenix:jfoenix:9.0.10")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")

    compileOnly("org.projectlombok", "lombok", lombok.version)

    testImplementation("com.hynnet:jacob:1.18")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.20.2")
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.jar {
    val mainClass = "com.github.otymko.phoenixbsl.PhoenixLauncher"
    manifest {
        attributes["Main-Class"] = mainClass
        attributes["Implementation-Version"] = project.version
        attributes["Arguments"] = javaOptionsForJavaFx
    }

    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaExec> {
    jvmArgs = javaOptionsForJavaFx.split(" ")
}

tasks.shadowJar {
    project.configurations.implementation.get().isCanBeResolved = true
    configurations = listOf(project.configurations["implementation"])
    archiveClassifier.set("")
}

tasks.register<Exec>("jpackage") {
    dependsOn(tasks.shadowJar)
    val jpackage = "jpackage"
    executable(jpackage)
    args(
            "--name", "phoenixbsl",
            "--type", "msi",
            "--input", "build/libs",
            "--main-jar", "phoenix-$version.jar",
            "--java-options", javaOptionsForJavaFx,
            "--win-dir-chooser",
            "--win-shortcut",
            "--win-menu",
            "--app-version", semver,
            "--vendor", "otymko",
    )
}

sonar {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "otymko ")
        property("sonar.projectKey", "phoenixbsl")
        property("sonar.projectName", "Phoenix BSL")
        property("sonar.exclusions", "**/gen/**/*.*")
    }
}

javafx {
    version = "17"
    modules("javafx.controls", "javafx.fxml")
}

lombok {
    version = "1.18.20"
}

/* Получить версия проекта без дополнительной информации
(только major, minor, patch)
 */
fun calculateVersion(prefix: String?, withSnapshot: Boolean): String? {
    val repository = GitRepository(GitCommandsFactory(project.projectDir.absolutePath))
    val calculator = GitVersionCalculator(repository)
    val semver = calculator.calculateSemVer(prefix, withSnapshot)
    return String.format("%d.%d.%d", semver.major, semver.minor, semver.patch)
}
