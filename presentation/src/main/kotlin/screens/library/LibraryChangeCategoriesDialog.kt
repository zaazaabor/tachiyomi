/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.bluelinelabs.conductor.Controller
import tachiyomi.domain.category.model.Category
import tachiyomi.ui.R
import tachiyomi.ui.controller.DialogController
import tachiyomi.ui.screens.category.getVisibleName

class LibraryChangeCategoriesDialog<T>(
  bundle: Bundle? = null
) : DialogController(bundle) where T : Controller, T : LibraryChangeCategoriesDialog.Listener {

  private var mangas: Collection<Long> = emptyList<Long>()

  private var allCategories = emptyList<Category>()

  private var preselected = emptyArray<Int>()

  constructor(
    target: T,
    mangas: Collection<Long>,
    allCategories: List<Category>,
    preselected: Array<Int>
  ) : this() {

    this.mangas = mangas
    this.allCategories = allCategories.filterNot { it.isAll }
    this.preselected = preselected
    targetController = target
  }

  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    return MaterialDialog(activity!!)
      .title(R.string.action_move_category)
      .listItemsMultiChoice(
        items = allCategories.map { it.getVisibleName(activity!!) },
        initialSelection = preselected.toIntArray()
      ) { _, indices, _ ->
        val newCategories = indices.map { allCategories[it].id }
        (targetController as? Listener)?.updateCategoriesForMangas(newCategories, mangas)
      }
      .positiveButton()
      .negativeButton()
  }

  interface Listener {
    fun updateCategoriesForMangas(categoryIds: Collection<Long>, mangaIds: Collection<Long>)
  }

}
