plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "2.2.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "com.marvelousanything"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation(kotlin("reflect"))
    implementation("io.prometheus:prometheus-metrics-core:1.0.0")
    implementation("io.prometheus:prometheus-metrics-instrumentation-jvm:1.0.0")
    implementation("io.prometheus:prometheus-metrics-exporter-httpserver:1.0.0")
}

kotlin {
    jvmToolchain(17)
}

configurations {
    shadow
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.release.set(17)
    }

    build {
        dependsOn("shadowJar")
        dependsOn(reobfJar)
    }

    runServer {
        minecraftVersion("1.20.1")
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "com.marvelousanything.PacketSnifferPlugin"
    prefix = "PacketSniffer"
    apiVersion = "1.20"
    authors = listOf("MarvelousAnything")
}