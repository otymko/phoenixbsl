
import java.net.URI

plugins {
    java
    maven
    jacoco
    id("com.github.gradle-git-version-calculator") version "1.1.0"
    id("com.github.ben-manes.versions") version "0.22.0"
}

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
    flatDir {
        dirs("libs")
    }
}

group = "org.github.otymko.phoenixbsl"
version = "0.1"

dependencies {

    compile("com.hynnet", "jacob", "1.18")
    compile("com.github.mmarquee:ui-automation:develop-SNAPSHOT")
    compile("com.github.1c-syntax:bsl-language-server:0.10.2")
    testCompile("junit", "junit", "4.12")
    compile("lc.kra.system:system-hook:3.5");

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.github.otymko.phoenixbsl.Launcher"
        attributes["Implementation-Version"] = archiveVersion.get()
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