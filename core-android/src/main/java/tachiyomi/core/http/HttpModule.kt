package tachiyomi.core.http

import toothpick.config.Module

object HttpModule : Module() {

  init {
    bind(Http::class.java).toProvider(HttpProvider::class.java).providesSingletonInScope()
  }
}
