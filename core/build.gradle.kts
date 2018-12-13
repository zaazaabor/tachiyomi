plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
}

dependencies {
  api(Deps.kotlin.stdlib)
  api(Deps.kotlin.stdlib)
  api(Deps.rxJava)
  api(Deps.okhttp)
  api(Deps.jsoup)
  api(Deps.coroutines.core)

  implementation(Deps.toothpick.runtime)
}
