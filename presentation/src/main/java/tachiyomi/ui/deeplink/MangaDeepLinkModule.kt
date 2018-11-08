package tachiyomi.ui.deeplink

import tachiyomi.core.di.bindInstance
import toothpick.config.Module

class MangaDeepLinkModule(controller: MangaDeepLinkController) : Module() {
  init {
    val params = MangaDeepLinkParams(
      controller.getMangaKey(),
      controller.getSourceId()
    )
    bindInstance(params)
  }
}
