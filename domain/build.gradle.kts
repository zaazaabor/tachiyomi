plugins {
  id("java-library")
  id("kotlin")
  id("kotlin-kapt")
  id("kotlinx-serialization")
}

dependencies {
  implementationProject(Projects.common)
  implementationProject(Projects.sourceApi)

  implementation(Deps.toothpick.runtime)
  kapt(Deps.toothpick.compiler)

  testImplementation(Deps.junit)
  testImplementation(Deps.mockito)
  testImplementation(Deps.mockitokt)
  testImplementation(Deps.toothpick.testing)
  kaptTest(Deps.toothpick.compiler)
}
