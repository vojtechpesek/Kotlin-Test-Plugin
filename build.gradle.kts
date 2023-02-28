plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.intellij") version "1.10.2"
}

group = "eu.livesport"
version = "0.5.1"

repositories {
    mavenCentral()
}

intellij {
    // Use https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html for correct feature set and runtime
    version.set("223.8617.56")

    // Use IntelliJ IDEA CE because it's the basis of the IntelliJ Platform:
    type.set("IC")

    // Require the Android plugin (Gradle will choose the correct version):
    plugins.set(listOf("android", "java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    patchPluginXml {
        sinceBuild.set("221.6008.13")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(File(System.getenv("CERTIFICATE_CHAIN") ?: "./.keys/chain.crt").readText(Charsets.UTF_8))
        certificateChain.set(File(System.getenv("PRIVATE_KEY") ?: "./.keys/private.pem").readText(Charsets.UTF_8))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runIde {
        jvmArgs = listOf("-Xmx4g", "-Xms1g")
        ideDir.set(file("/Users/vojtech.pesek/Library/Application Support/JetBrains/Toolbox/apps/AndroidStudio/ch-1/223.8617.56.2231.9644228/Android Studio Preview.app/Contents"))
    }
}
