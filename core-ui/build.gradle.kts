plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-android-extensions")
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
  implementationProject(Projects.common)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.recyclerView)
  implementation(Deps.androidX.preference)
  implementation(Deps.androidKTX)
  implementation(Deps.coRedux)
  implementation(Deps.coReduxLog)
  implementation(Deps.conductor)
  implementation(Deps.conductorPreference)
  implementation(Deps.cyanea)

  implementation(Deps.glide.core)
  kapt(Deps.glide.compiler)
}

androidExtensions {
  isExperimental = true
}
