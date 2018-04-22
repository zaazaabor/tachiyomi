package tachiyomi.core.http

import toothpick.config.Module

/**
 * A [toothpick.Toothpick] module to register the HTTP dependencies available to the application.
 */
object HttpModule : Module() {

  init {
    bind(Http::class.java).toProvider(HttpProvider::class.java).providesSingletonInScope()
    bind(JSFactory::class.java).to(DuktapeJSFactory::class.java).singletonInScope()
  }
}
