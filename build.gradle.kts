import java.net.URI

import com.github.gradle_git_version_calculator.GitRepository;
import com.github.gradle_git_version_calculator.GitCommandsFactory;
import com.github.gradle_git_version_calculator.GitVersionCalculator;


plugins {
    java
    maven
    jacoco
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.sonarqube") version "2.8"
    id("io.franzbecker.gradle-lombok") version "3.2.0"
    id("com.github.gradle-git-version-calculator") version "1.1.0"
}

repositories {
    flatDir {
        dirs("libs")
    }
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

group = "com.github.otymko.phoenixbsl"
version = gitVersionCalculator.calculateVersion("v")

dependencies {
    testImplementation("com.hynnet", "jacob", "1.18")
    testImplementation("junit", "junit", "4.12")
    testImplementation("org.assertj", "assertj-core", "3.16.1")

    implementation("net.java.dev.jna:jna-platform:5.4.0")
    implementation("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.8.1")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("lc.kra.system","system-hook", "3.5")

    // ui
    implementation("com.jfoenix","jfoenix", "9.0.9")

    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.10.2")

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
        attributes["Main-Class"] = "com.github.otymko.phoenixbsl.PhoenixLauncher"
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
    var semver = calculateVersion("v", false)
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
            "--app-version", semver,
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

/* Получить версия проекта без дополнительной информации
(только major, minor, patch)
 */
fun calculateVersion(prefix: String?, withSnapshot: Boolean): String? {
    val repository = GitRepository(GitCommandsFactory(project.projectDir.absolutePath))
    val calculator = GitVersionCalculator(repository)
    val semver = calculator.calculateSemVer(prefix, withSnapshot)
    return String.format("%d.%d.%d", semver.major, semver.minor, semver.patch)
}
