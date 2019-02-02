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

  implementation(Deps.kotlin.stdlib)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.recyclerView)
  implementation(Deps.androidKTX)
  implementation(Deps.conductor)

  implementation(Deps.glide.core)
  implementation(Deps.glide.okhttp)
  kapt(Deps.glide.compiler)
}
