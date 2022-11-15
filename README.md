# Kotlin Test Plugin
Plugin for Android Studio to better support writing tests for multiplatform: https://kotlinlang.org/api/latest/kotlin.test/

The plugin defines several file templates and uses them to generate test class with option to configure it trough Generate Test dialog interface. It is currently in early alpha phase and is published in the alpha channel on JetBrains Marketplace: https://plugins.jetbrains.com/plugin/20160-kotlin-test.

## Current limitations
At the moment the plugin is unable to generate test for the iosTest folder, since the default action cannot offer any suitable folders. Therefore, the dialog never finishes and the plugin never receives callback. See [issue #1](https://github.com/vojtechpesek/Kotlin-Test-Plugin/issues/1)

The **Generate** action inside existing test still produces incorrect annotations. See [issue #2](https://github.com/vojtechpesek/Kotlin-Test-Plugin/issues/2)