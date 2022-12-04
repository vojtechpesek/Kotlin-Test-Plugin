plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "eu.livesport"
version = "0.4.0"

repositories {
    mavenCentral()
}

intellij {
    // Use https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html for correct feature set and runtime
    version.set("221.6008.13")

    // Use IntelliJ IDEA CE because it's the basis of the IntelliJ Platform:
    type.set("IC")

    // Require the Android plugin (Gradle will choose the correct version):
    plugins.set(listOf("android", "java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    patchPluginXml {
        sinceBuild.set("213.7172.25")
        untilBuild.set("222.*")
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
        ideDir.set(file("/Users/vojtech.pesek/Library/Application Support/JetBrains/Toolbox/apps/AndroidStudio/ch-2/221.6008.13.2211.9237616/Android Studio Preview.app/Contents"))
    }
}
