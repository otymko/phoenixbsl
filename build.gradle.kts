import java.net.URI

plugins {
    java
    maven
    jacoco
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

group = "org.github.otymko.phoenixbsl"
version = "0.3.0"

dependencies {
    compile("com.hynnet", "jacob", "1.18")
    testCompile("junit", "junit", "4.12")
    implementation("net.java.dev.jna:jna-platform:5.4.0")
    compile("net.java.dev.jna:jna-platform:5.4.0")
    compile("com.1stleg:jnativehook:2.1.0")
    compile("org.eclipse.lsp4j", "org.eclipse.lsp4j", "0.8.1")

    compile("org.slf4j", "slf4j-api", "1.8.0-beta4")
    compile("org.slf4j", "slf4j-simple", "1.8.0-beta4")
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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.github.otymko.phoenixbsl.LauncherApp"
        attributes["Implementation-Version"] = "0.3.0"
    }
    configurations["compile"].forEach {
        from(zipTree(it.absoluteFile)) {
            exclude("META-INF/MANIFEST.MF")
            exclude("META-INF/*.SF")
            exclude("META-INF/*.DSA")
            exclude("META-INF/*.RSA")
        }
    }
}

javafx {
    version = "11"
    modules("javafx.controls")
}
