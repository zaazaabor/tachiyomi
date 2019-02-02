plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
  compileSdkVersion(Config.compileSdk)
  defaultConfig {
    minSdkVersion(Config.minSdk)
    targetSdkVersion(Config.targetSdk)
  }
  sourceSets["main"].java.srcDirs("src/main/kotlin")
}

dependencies {
  implementationProject(Projects.coreAndroid)
  implementationProject(Projects.domain)
  implementationProject(Projects.sourceApi)

  implementation(Deps.storio)
  implementation(Deps.rxJava)
  implementation(Deps.rxAndroid)
  implementation(Deps.rxRelay)
  implementation(Deps.kotlin.stdlib)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.gson)
  implementation(Deps.kotson)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.smoothie)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.androidX.appCompat)
}
