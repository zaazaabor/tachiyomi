/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.catalogs_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.domain.catalog.model.CatalogLocal
import tachiyomi.domain.catalog.model.InstallStep
import tachiyomi.ui.R
import tachiyomi.ui.catalogbrowse.CatalogBrowseController
import tachiyomi.ui.catalogdetail.CatalogDetailsController
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.controller.withHorizontalTransition
import tachiyomi.ui.glide.GlideController
import tachiyomi.ui.glide.GlideProvider
import tachiyomi.ui.home.HomeChildController

class CatalogController : MvpController<CatalogsPresenter>(),
  CatalogAdapter.Listener,
  HomeChildController,
  GlideController {

  private var adapter: CatalogAdapter? = null

  override val glideProvider = GlideProvider.from(this)

  override fun getPresenterClass() = CatalogsPresenter::class.java

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.catalogs_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    adapter = CatalogAdapter(view.context, this, glideProvider.get())
    catalogs_recycler.adapter = adapter
    catalogs_recycler.addItemDecoration(CatalogDividerDecoration(view.context))

    catalogs_swipe_refresh.setOnRefreshListener { onSwipeRefresh() }

    presenter.state
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    //catalogs_recycler.adapter = null
    adapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.items !== prevState?.items || state.installingCatalogs !== prevState.installingCatalogs) {
      renderItems(state.items, state.installingCatalogs)
    }
    if (state.isRefreshing != prevState?.isRefreshing) {
      renderIsRefreshing(state.isRefreshing)
    }
  }

  private fun renderItems(
    catalogs: List<Any>,
    installingCatalogs: Map<String, InstallStep>
  ) {
    adapter?.submitItems(catalogs, installingCatalogs)
  }

  private fun renderIsRefreshing(isRefreshing: Boolean) {
    catalogs_swipe_refresh.isRefreshing = isRefreshing
  }

  //===========================================================================
  // ~ User interaction
  //===========================================================================

  override fun onCatalogClick(catalog: Catalog) {
    val id = when (catalog) {
      is CatalogLocal -> catalog.source.id
      else -> return
    }
    router.pushController(CatalogBrowseController(id).withHorizontalTransition())
  }

  override fun onLanguageChoiceClick(languageChoice: LanguageChoice) {
    presenter.setLanguageChoice(languageChoice)
  }

  override fun onInstallClick(catalog: Catalog) {
    presenter.installCatalog(catalog)
  }

  override fun onSettingsClick(catalog: Catalog) {
    if (catalog !is CatalogInstalled) return
    router.pushController(CatalogDetailsController(catalog.pkgName).withHorizontalTransition())
  }

  private fun onSwipeRefresh() {
    presenter.refreshCatalogs()
  }

}
