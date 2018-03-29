package tachiyomi.ui.library

import toothpick.config.Module

class LibraryModule(private val controller: LibraryController) : Module() {

  init {
    bind(LibraryPresenter::class.java).singletonInScope()
  }

}
