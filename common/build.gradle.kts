plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.serialization)
  api(Deps.rxJava)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)
  api(Deps.coroutines.rx2)
  api(Deps.timber.jdk)

  implementation(Deps.toothpick.runtime)
}
