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
  implementationProject(Projects.core)
  implementationProject(Projects.domain)
  implementationProject(Projects.sourceApi)

  implementation(Deps.storio)
  implementation(Deps.sqlite)
  implementation(Deps.rxJava)
  implementation(Deps.rxAndroid)
  implementation(Deps.rxRelay)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.kotson)
  implementation(Deps.workManager.runtime)
  implementation(Deps.workManager.rx2)

  implementation(Deps.toothpick.runtime)
  implementation(Deps.toothpick.smoothie)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.androidX.appCompat)
}
