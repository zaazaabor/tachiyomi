/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.cyanea

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.R
import com.jaredrummler.cyanea.app.BaseCyaneaActivity
import com.jaredrummler.cyanea.prefs.CyaneaTheme
import tachiyomi.ui.controller.BaseController

/**
 * Controller containing the theme picker
 */
class CyaneaThemePickerController : BaseController(), OnItemClickListener {

  val cyanea get() = (activity as? BaseCyaneaActivity)?.cyanea ?: Cyanea.instance

  open val themesJsonAssetPath get() = "themes/cyanea_themes.json"

  private var gridView: GridView? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.cyanea_theme_picker, container, false)
  }

  override fun onViewCreated(view: View) {
    super.onViewCreated(view)
    val gridView = view.findViewById<GridView>(R.id.gridView)
    this.gridView = gridView
    val themes = CyaneaTheme.from(view.context.assets, themesJsonAssetPath)
    gridView.adapter = CyaneaThemePickerAdapter(themes, cyanea, LayoutInflater.from(activity!!))
    gridView.onItemClickListener = this
    scrollToCurrentTheme(themes)
  }

  override fun onDestroyView(view: View) {
    gridView = null
    super.onDestroyView(view)
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val gridView = gridView ?: return
    val theme = (gridView.adapter as CyaneaThemePickerAdapter).getItem(position)
    theme.apply(cyanea).recreate(activity!!, smooth = true)
  }

  private fun scrollToCurrentTheme(themes: List<CyaneaTheme>) {
    var selectedTheme = -1
    val gridView = gridView ?: return
    run {
      themes.forEachIndexed { index, theme ->
        if (theme.isMatchingColorScheme(cyanea)) {
          selectedTheme = index
          return@run
        }
      }
    }
    if (selectedTheme != -1) {
      gridView.post {
        if (selectedTheme < gridView.firstVisiblePosition || selectedTheme > gridView.lastVisiblePosition) {
          gridView.setSelection(selectedTheme)
        }
      }
    }
  }

}
