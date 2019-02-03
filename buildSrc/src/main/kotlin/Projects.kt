import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

enum class Projects(val path: String) {
  // Java modules
  common(":common"),
  domain(":domain"),
  sourceApi(":source-api"),

  // Android modules
  core(":core"),
  coreUi(":core-ui"),
  `data`(":data"),
  ui(":ui"),
  app(":app")
}

fun DependencyHandler.apiProject(lib: Projects) {
  add("api", project(lib.path))
}

fun DependencyHandler.implementationProject(lib: Projects) {
  add("implementation", project(lib.path))
}
