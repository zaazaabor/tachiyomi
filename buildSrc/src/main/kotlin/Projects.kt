import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

enum class Projects(val path: String) {
  core(":core"),
  coreAndroid(":core-android"),
  domain(":domain"),
  `data`(":data"),
  ui(":ui"),
  app(":app"),
  sourceApi(":source-api"),
  sourceDeepLink(":source-deeplink")
}

fun DependencyHandler.apiProject(lib: Projects) {
  add("api", project(lib.path))
}

fun DependencyHandler.implementationProject(lib: Projects) {
  add("implementation", project(lib.path))
}
