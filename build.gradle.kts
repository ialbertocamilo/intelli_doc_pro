plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.valtecna"
version = "1.0-nightly"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
    }

    // AWS SDK for Bedrock
    implementation("software.amazon.awssdk:bedrockruntime:2.29.41")
    implementation("software.amazon.awssdk:auth:2.29.41")
    implementation("software.amazon.awssdk:regions:2.29.41")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.jsoup:jsoup:1.17.2")

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }

    signing {
        certificateChain.set(file("chain.crt").readText())
        privateKey.set(file("private.pem").readText())
        password.set(System.getenv("PRIVATE_KEY_PASSWORD") ?: "")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    // Configure test task
    test {
        useJUnit()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }
}

kotlin {
    jvmToolchain(21)
}
