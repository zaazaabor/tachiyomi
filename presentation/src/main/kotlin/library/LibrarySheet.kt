/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.library

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.library_sheet.view.*
import tachiyomi.ui.R

class LibrarySheet @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {

  fun init(adapter: LibrarySheetAdapter) {
    val recycler = library_sheet_recycler
    recycler.adapter = adapter
    recycler.layoutManager = FlexboxLayoutManager(context)
  }

  companion object {
    fun show(activity: Activity, adapter: LibrarySheetAdapter): BottomSheetDialog {
      val sheet = BottomSheetDialog(activity)
      val view = LayoutInflater.from(activity)
        .inflate(R.layout.library_sheet, null) as LibrarySheet
      sheet.setContentView(view)
      view.init(adapter)
      sheet.show()
      return sheet
    }
  }

}
