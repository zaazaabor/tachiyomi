package tachiyomi.ui.catalogbrowse

import toothpick.config.Module

/**
 * Module used to inject the dependencies of [CatalogBrowsePresenter] from a
 * [CatalogBrowseController].
 */
class CatalogBrowseModule(controller: CatalogBrowseController) : Module() {

  init {
    val params = CatalogBrowseParams(controller.getSourceId())
    bind(CatalogBrowseParams::class.javaObjectType).toInstance(params)
  }

}
