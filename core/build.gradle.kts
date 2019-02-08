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
  apiProject(Projects.common)

  implementation(Deps.duktape)
  implementation(Deps.rxPreferences)
  implementation(Deps.rxAndroid)
  implementation(Deps.storio)
  implementation(Deps.androidX.core)
  implementation(Deps.coroutines.core)
  implementation(Deps.coroutines.android)
  implementation(Deps.coroutines.rx2)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)
}
