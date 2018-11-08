package tachiyomi.ui.deeplink

import tachiyomi.core.di.bindInstance
import toothpick.config.Module

class ChapterDeepLinkModule(controller: ChapterDeepLinkController) : Module() {
  init {
    val params = ChapterDeepLinkParams(
      chapterKey = controller.getChapterKey(),
      sourceId = controller.getSourceId()
    )
    bindInstance(params)
  }
}
