package tachiyomi.ui.catalogbrowse

import toothpick.config.Module

class CatalogBrowseModule(private val controller: CatalogBrowseController) : Module() {

  init {
    bind(Long::class.javaObjectType).toInstance(controller.getSourceId())
  }

}
