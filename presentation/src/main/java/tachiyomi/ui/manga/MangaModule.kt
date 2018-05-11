package tachiyomi.ui.manga

import toothpick.config.Module

class MangaModule(private val controller: MangaController) : Module() {

  init {
    bind(Long::class.javaObjectType).toInstance(controller.getMangaId())
  }

}
