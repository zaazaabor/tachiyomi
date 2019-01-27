/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.catalogdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.catalogdetail_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.domain.catalog.model.CatalogInstalled
import tachiyomi.glide.GlideApp
import tachiyomi.ui.R
import tachiyomi.ui.base.MvpController
import tachiyomi.ui.home.HomeChildController
import tachiyomi.util.inflate
import java.util.Locale

class CatalogDetailsController(
  bundle: Bundle
) : MvpController<CatalogDetailsPresenter>(bundle),
  HomeChildController {

  constructor(pkgName: String) : this(Bundle().apply {
    putString(PKGNAME_KEY, pkgName)
  })

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  /**
   * Returns the presenter class used by this controller.
   */
  override fun getPresenterClass() = CatalogDetailsPresenter::class.java

  /**
   * Returns the module of this controller that provides the dependencies of the presenter.
   */
  override fun getModule() = CatalogDetailsModule(this)

  /**
   * Returns the package name of the catalog stored in the [Bundle] of this controller.
   */
  fun getCatalogPkgName() = args.getString(PKGNAME_KEY)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return container.inflate(R.layout.catalogdetail_controller)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    // Setup back navigation
    setupToolbarIconWithHomeController(catalogdetail_toolbar)
    RxToolbar.navigationClicks(catalogdetail_toolbar)
      .subscribeWithView { router.handleBack() }

    catalogdetail_uninstall_button.clicks()
      .subscribeWithView { uninstallCatalog() }

    presenter.stateObserver
      .scanWithPrevious()
      .subscribeWithView { (state, prevState) -> render(state, prevState) }
  }

  private fun render(state: CatalogDetailsViewState, prevState: CatalogDetailsViewState?) {
    if (state.isUninstalled) {
      router.popController(this)
    }
    if (state.catalog != null && state.catalog != prevState?.catalog) {
      renderCatalog(state.catalog)
    }
  }

  private fun renderCatalog(catalog: CatalogInstalled) {
    catalogdetail_title.text = catalog.name
    catalogdetail_version.text = catalog.versionName
    catalogdetail_lang.text = Locale(catalog.source.lang).displayLanguage
    catalogdetail_pkg.text = catalog.pkgName

    GlideApp.with(activity!!)
      .load(catalog)
      .into(catalogdetail_icon)
  }

  //===========================================================================
  // ~ User actions
  //===========================================================================

  private fun uninstallCatalog() {
    presenter.uninstallCatalog()
  }

  private companion object {
    const val PKGNAME_KEY = "pkgname"
  }

}
