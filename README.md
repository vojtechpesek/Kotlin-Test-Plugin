# Kotlin Test Plugin
Plugin for Android Studio to better support writing tests for multiplatform: https://kotlinlang.org/api/latest/kotlin.test/

The plugin defines several file templates and uses them to generate test class with option to configure it trough Generate Test dialog interface. It is currently in early alpha phase, and it is not yet published.

## Roadmap
- [x] Generate Test Class using Dialog with selectable methods
- [x] SetUp and Teardown methods
- [x] Shorten annotation using imports
- [x] Select commonMain as source folder
- [x] Create icon
- [ ] Publish plugin on IntelliJ Plugin site

## Current limitations
At the moment the plugin is unable to generate test for the iosTest folder, since the default action cannot offer any suitable folders. Therefore, the dialog never finishes and the plugin never receives callback. 

The **Generate** action inside existing test still produces incorrect annotations. IntelliJ Platform thinks for some reason, that the Kotlin Test Plugin is not applicable. This is probably due to poor knowledge of the framework, rather than a problem in the platform itself.