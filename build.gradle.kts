buildscript {
  repositories {
    google()
    jcenter()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:3.3.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin.version}")
  }
}

plugins {
  id("com.github.ben-manes.versions") version "0.20.0"
}

allprojects {
  repositories {
    google()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://google.bintray.com/flexbox-layout") }
    jcenter()
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
