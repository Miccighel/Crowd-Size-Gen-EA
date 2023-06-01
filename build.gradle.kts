plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.intellij") version "1.1.6"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "uniud.smdc"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "15"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("commons-cli:commons-cli:1.5.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.uma.jmetal:jmetal-problem:5.10")
    implementation("org.uma.jmetal:jmetal-algorithm:5.10")
    implementation("org.uma.jmetal:jmetal-core:5.10")
    implementation("org.uma.jmetal:jmetal-exec:5.9")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.github.holgerbrandl:krangl:0.17.1")
    implementation("com.google.code.gson:gson:2.8.9")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.2.2")
}

tasks {

    patchPluginXml {
        changeNotes.set(
            """
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent()
        )
    }

    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "Program"))
        }
    }

}
