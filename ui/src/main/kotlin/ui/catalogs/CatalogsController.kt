/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.catalogs_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.catalog.model.Catalog
import tachiyomi.ui.R
import tachiyomi.ui.base.MvpController
import tachiyomi.ui.base.withFadeTransition
import tachiyomi.ui.catalogbrowse.CatalogBrowseController
import tachiyomi.ui.home.HomeChildController

class CatalogsController : MvpController<CatalogsPresenter>(),
  CatalogsAdapter.Listener,
  HomeChildController {

  private var adapter: CatalogsAdapter? = null

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
    adapter = CatalogsAdapter(this)
    catalogs_recycler.layoutManager = LinearLayoutManager(view.context)
    catalogs_recycler.adapter = adapter

    presenter.state
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> dispatch(state, prevState) }
  }

  override fun onDestroyView(view: View) {
    adapter = null
    super.onDestroyView(view)
  }

  //===========================================================================
  // ~ Render
  //===========================================================================

  private fun dispatch(state: CatalogsViewState, prevState: CatalogsViewState?) {
    if (state.installed !== prevState?.installed || state.internal !== prevState.internal) {
      renderBrowsableCatalogs(state.internal + state.installed)
    }
  }

  private fun renderBrowsableCatalogs(catalogs: List<Catalog>) {
    adapter?.submitBrowsableCatalogs(catalogs)
  }

  //===========================================================================
  // ~ User interaction
  //===========================================================================

  override fun onRowClick(catalog: Catalog) {
    // TODO
    val id = when (catalog) {
      is Catalog.Installed -> catalog.source.id
      else -> return
    }
    router.pushController(CatalogBrowseController(id).withFadeTransition())
  }

}
