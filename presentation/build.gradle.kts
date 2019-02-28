import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension

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
  implementationProject(Projects.core)
  implementationProject(Projects.coreUi)
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)
  implementationProject(Projects.data)

  implementation(Deps.androidX.design)
  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.recyclerView)
  implementation(Deps.androidX.preference)
  implementation(Deps.androidX.card)
  implementation(Deps.androidX.emoji)
  implementation(Deps.constraint)

  implementation(Deps.androidKTX)

  implementation(Deps.conductor)
  implementation(Deps.conductorPreference)

  implementation(Deps.rxJava)
  implementation(Deps.rxKotlin)
  implementation(Deps.rxRelay)
  implementation(Deps.rxRedux)
  implementation(Deps.rxBinding.platform)
  implementation(Deps.rxBinding.support)
  implementation(Deps.rxBinding.appcompat)

  implementation(Deps.kotlin.stdlib)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  implementation(Deps.materialDimens)
  implementation(Deps.materialDialog.core)
  implementation(Deps.materialDialog.input)
  implementation(Deps.cyanea)

  implementation(Deps.glide.core)
  implementation(Deps.glide.okhttp)

  implementation(Deps.flexbox)
}

androidExtensions {
  configure(delegateClosureOf<AndroidExtensionsExtension> {
    isExperimental = true
  })
}
