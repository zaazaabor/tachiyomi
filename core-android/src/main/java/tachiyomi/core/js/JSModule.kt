package tachiyomi.core.js

import toothpick.config.Module

object JSModule : Module() {

  init {
    bind(JSFactory::class.java).to(DuktapeJSFactory::class.java).singletonInScope()
  }
}
