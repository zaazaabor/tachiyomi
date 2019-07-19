/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.settings_sync_controller.*
import kotlinx.coroutines.flow.asFlow
import tachiyomi.ui.R
import tachiyomi.ui.controller.MvpController
import tachiyomi.ui.util.clicks
import tachiyomi.ui.util.navigationClicks
import tachiyomi.ui.util.scanWithPrevious
import tachiyomi.ui.util.visibleIf

class SyncController : MvpController<SyncPresenter>() {

  override fun getPresenterClass() = SyncPresenter::class.java

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.settings_sync_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    sync_toolbar.navigationClicks()
      .collectWithView { router.handleBack() }

    sync_login.clicks()
      .collectWithView { handleLogin() }

    presenter.state.asFlow()
      .scanWithPrevious()
      .collectWithView { (state, prevState) -> render(state, prevState) }
  }

  private fun render(state: ViewState, prevState: ViewState?) {
    if (state.isLoading != prevState?.isLoading) {
      renderIsLoading(state.isLoading)
    }
    if (state.isLogged != prevState?.isLogged) {
      renderIsLogged(state.isLogged)
    }
  }

  private fun renderIsLoading(isLoading: Boolean) {
    sync_progress.visibleIf { isLoading }
  }

  private fun renderIsLogged(isLogged: Boolean) {

  }

  private fun handleLogin() {
    presenter.login(
      sync_host.editText!!.text.toString(),
      sync_user.editText!!.text!!.toString(),
      sync_pass.editText!!.text.toString()
    )
  }

}
