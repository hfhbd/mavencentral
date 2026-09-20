# maven-central

Project isolation compatible publishing plugin,
supporting [the new Central Portal Publishing API](https://central.sonatype.org/publish/publish-portal-api/) only.

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

## Setup

Apply the plugin in each project.

```kotlin
// build.gradle (.kts)
plugins {
    id("io.github.hfhbd.mavencentral") version "LATEST"
}
```

and apply the upload plugin in 1 project, e.g. the root project and add a projects that should be published in the
extension:

```kotlin
// build.gradle (.kts)
plugins {
    id("io.github.hfhbd.mavencentral.upload") version "LATEST"
}

mavenCentral {
    dependencies {
        publishToMavenCentral(projects.foo)
        publishToMavenCentral(projects.bar)
    }
}
```

### Single project build

If you don't use subprojects and only the root project, you can just apply both plugins without adding a dependency:

```kotlin
plugins {
    id("io.github.hfhbd.mavencentral") version "LATEST"
    id("io.github.hfhbd.mavencentral.upload") version "LATEST"
}
```

### Publish all projects

If you want to publish all projects, you can also apply the `io.github.hfhbd.mavencentral.upload.all` settings plugin.

## Usage

You need to configure the publications using the core `maven-publish` and `signing` plugins.

To publish the publications, call
`./gradlew publishToMavenCentral -PmavenCentralUsername=MYUSERNAME -PmavenCentralPassword=MYSECRETPASSWORD`.
Publishing uses the automatic behavior.
