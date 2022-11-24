# Kotlin Test Plugin
Plugin for Android Studio to better support writing tests for multiplatform: https://kotlinlang.org/api/latest/kotlin.test/

The plugin defines several file templates and uses them to generate test class with option to configure it trough Generate Test dialog interface. It is currently in beta phase and is published on JetBrains Marketplace: https://plugins.jetbrains.com/plugin/20160-kotlin-test. Try it out and let me know if you find any bugs or have feature requests. 

## Current limitations
At the moment the plugin is unable to generate test for the `iosTest` folder, since the default action cannot offer any suitable folders. Therefore, the dialog never finishes and the plugin never receives callback. See [issue #1](https://github.com/vojtechpesek/Kotlin-Test-Plugin/issues/1).