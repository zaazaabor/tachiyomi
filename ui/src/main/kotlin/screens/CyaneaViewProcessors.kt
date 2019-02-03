/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens

import android.app.Activity
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.inflator.CyaneaViewProcessor
import tachiyomi.ui.R
import tachiyomi.ui.util.getResourceId

fun Activity.getCyaneaViewProcessors(): Array<CyaneaViewProcessor<*>> {
  return arrayOf(
    BottomNavigationViewProcessor()
  )
}

private class BottomNavigationViewProcessor : CyaneaViewProcessor<BottomNavigationView>() {

  override fun getType(): Class<BottomNavigationView> = BottomNavigationView::class.java

  override fun process(view: BottomNavigationView, attrs: AttributeSet?, cyanea: Cyanea) {
    val checkedColor = if (cyanea.isActionBarLight) {
      ContextCompat.getColor(view.context, R.color.textColorPrimary)
    } else {
      ContextCompat.getColor(view.context, R.color.textColorPrimaryInverse)
    }
    val uncheckedColor = ColorUtils.setAlphaComponent(checkedColor, 128)

    val colorState = ColorStateList(
      arrayOf(
        intArrayOf(-android.R.attr.state_checked),
        intArrayOf()
      ),
      intArrayOf(uncheckedColor, checkedColor)
    )

    val itemBackgroundRes = ContextThemeWrapper(view.context, if (cyanea.isActionBarLight) {
      R.style.Theme_MaterialComponents_Light_NoActionBar
    } else {
      R.style.Theme_MaterialComponents_NoActionBar
    }).getResourceId(R.attr.selectableItemBackgroundBorderless)

    view.setBackgroundColor(cyanea.primary)
    view.itemIconTintList = colorState
    view.itemTextColor = colorState
    view.itemBackgroundResource = itemBackgroundRes
  }

}
