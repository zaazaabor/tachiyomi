package tachiyomi.ui.category

import toothpick.config.Module

class CategoryModule : Module() {

  init {
    bind(CategoryPresenter::class.java).singletonInScope()
  }
}
