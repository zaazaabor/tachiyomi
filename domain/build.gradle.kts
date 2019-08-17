plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("kotlinx-serialization")
}

val test by tasks.getting(Test::class) {
  useJUnitPlatform { }
}

dependencies {
  implementationProject(Projects.common)
  implementationProject(Projects.sourceApi)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  testImplementation(Deps.junit)
  testImplementation(Deps.mockk)
  testImplementation(Deps.toothpick.testing)
  testImplementation(Deps.kotlintest)
  kaptTest(Deps.toothpick.compiler)
}
