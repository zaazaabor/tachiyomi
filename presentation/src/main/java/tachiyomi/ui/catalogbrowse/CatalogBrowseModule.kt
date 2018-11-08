package tachiyomi.ui.catalogbrowse

import tachiyomi.core.di.bindInstance
import toothpick.config.Module

/**
 * Module used to inject the dependencies of [CatalogBrowsePresenter] from a
 * [CatalogBrowseController].
 */
class CatalogBrowseModule(controller: CatalogBrowseController) : Module() {

  init {
    val params = CatalogBrowseParams(controller.getSourceId())
    bindInstance(params)
  }

}
