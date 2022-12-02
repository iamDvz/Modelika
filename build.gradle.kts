import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "ru.iamdvz"
version = "1.0-SNAPSHOT"
description = "Modelika"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.codemc.org/repository/maven-public/")
    maven(url = "https://repo.destroystokyo.com/repository/maven-public/")
    maven(url = "https://repo.citizensnpcs.co")
    maven(url = "https://repo.dmulloy2.net/repository/public/")
    maven(url = "https://repo.maven.apache.org/maven2/")
}

dependencies {
    compileOnly("com.ticxo.modelengine:api:R3.0.0")
    compileOnly("com.github.iamDvz:DivizionCore:8fb78d5f")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("com.github.TheComputerGeek2.MagicSpells:core:main-SNAPSHOT:dev")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("Modelika-${version}.jar")
}
tasks.withType<Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}
publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
