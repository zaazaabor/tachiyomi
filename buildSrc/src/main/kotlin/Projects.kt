
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

enum class Projects(val path: String) {
  core(":core"),
  coreAndroid(":core-android"),
  coreAndroidUi(":core-android-ui"),
  domain(":domain"),
  `data`(":data"),
  glide(":glide"),
  ui(":ui"),
  app(":app"),
  sourceApi(":source-api")
}

fun DependencyHandler.apiProject(lib: Projects) {
  add("api", project(lib.path))
}

fun DependencyHandler.implementationProject(lib: Projects) {
  add("implementation", project(lib.path))
}
