/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.manga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import kotlinx.android.synthetic.main.manga_controller.*
import tachiyomi.core.rx.scanWithPrevious
import tachiyomi.ui.R
import tachiyomi.ui.base.MvpController

class MangaController(
  bundle: Bundle? = null
) : MvpController<MangaPresenter>(bundle) {

  private var adapter: MangaAdapter? = null

  constructor(mangaId: Long) : this(Bundle().apply {
    putLong(MANGA_KEY, mangaId)
  })

  init {
    setHasOptionsMenu(true)
  }

  //===========================================================================
  // ~ Presenter
  //===========================================================================

  override fun getPresenterClass() = MangaPresenter::class.java

  override fun getModule() = MangaModule(this)

  fun getMangaId() = args.getLong(MANGA_KEY, -1)

  //===========================================================================
  // ~ Lifecycle
  //===========================================================================

  override fun onChangeStarted(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeStarted(changeHandler, changeType)
    setOptionsMenuHidden(!changeType.isEnter)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return inflater.inflate(R.layout.manga_controller, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)

    RxToolbar.navigationClicks(manga_toolbar)
      .subscribeWithView { router.handleBack() }

    adapter = MangaAdapter()
    manga_recycler.adapter = adapter
    manga_recycler.layoutManager = LinearLayoutManager(view.context)

    presenter.stateObserver
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

  private fun dispatch(state: MangaViewState, prevState: MangaViewState?) {
    if (state.header != prevState?.header) {
      renderHeader(state.header)
    }
  }

  private fun renderHeader(header: MangaHeader?) {
    manga_toolbar.title = header?.manga?.title.orEmpty()
    adapter?.submitList(listOf(header))
  }

  //===========================================================================
  // ~ Options menu
  //===========================================================================

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.manga, menu)
  }

  companion object {
    const val MANGA_KEY = "manga_id"
  }

}
