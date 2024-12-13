# maven-central

Project isolation compatible publishing plugin, supporting [the new Central Portal Publishing API](https://central.sonatype.org/publish/publish-portal-api/) only.

## Install

This package/Gradle plugin is uploaded to MavenCentral and GitHub packages.

```kotlin
// settings.gradle (.kts)
pluginManagement {
  repositories {
    mavenCentral()
  }
}
```

## Usage

Apply the plugin in each project.

```kotlin
// build.gradle (.kts)
plugins {
  id("io.github.hfhbd.mavencentral") version "LATEST"
}
```

You need to setup and configure the publications using the core `maven-publish` and `signing` plugins.

To publish the publications, call `./gradlew publishToMavenCentral -PmavenCentralUsername=MYUSERNAME -PmavenCentralPassword=MYSECRETPASSWORD`.
Publishing uses the automatic behavior.
