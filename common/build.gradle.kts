plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.serialization)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)
  api(Deps.timber.jdk)

  implementation(Deps.toothpick.runtime)
}
