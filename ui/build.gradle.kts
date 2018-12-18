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
  implementationProject(Projects.coreAndroid)
  implementationProject(Projects.sourceApi)
  implementationProject(Projects.domain)
  implementationProject(Projects.data)

  implementation(Deps.androidX.design)
  implementation(Deps.androidX.appCompat)
  implementation(Deps.androidX.recyclerView)
  implementation(Deps.androidX.card)
  implementation(Deps.androidX.emoji)
  implementation(Deps.constraint)

  implementation(Deps.androidKTX)

  implementation(Deps.conductor)

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
  implementation(Deps.materialDialog)
  implementation(Deps.cyanea)

  implementation(Deps.glide.core)
  implementation(Deps.glide.okhttp)
  kapt(Deps.glide.compiler)

  implementation(Deps.flexbox)
}

kapt {
  arguments {
    arg("toothpick_registry_package_name", "tachiyomi.ui")
  }
}

androidExtensions {
  configure(delegateClosureOf<AndroidExtensionsExtension> {
    isExperimental = true
  })
}
