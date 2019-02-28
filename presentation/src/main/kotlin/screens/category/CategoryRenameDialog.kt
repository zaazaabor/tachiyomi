/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.bluelinelabs.conductor.Controller
import tachiyomi.domain.category.Category
import tachiyomi.ui.R
import tachiyomi.ui.controller.DialogController

/**
 * Dialog to rename an existing category of the library.
 */
class CategoryRenameDialog<T>(bundle: Bundle? = null) : DialogController(bundle)
  where T : Controller, T : CategoryRenameDialog.Listener {

  /**
   * Id of the category to rename.
   */
  private var categoryId: Long? = null

  /**
   * Name of the new category. Value updated with each input from the user.
   */
  private var currentName = ""

  constructor(target: T, category: Category) : this() {
    targetController = target
    categoryId = category.id
    currentName = category.name
  }

  /**
   * Called when creating the dialog for this controller.
   *
   * @param savedViewState The saved state of this dialog.
   * @return a new dialog instance.
   */
  override fun onCreateDialog(savedViewState: Bundle?): Dialog {
    return MaterialDialog(activity!!)
      .title(R.string.action_rename_category)
      .input(
        hintRes = R.string.name,
        prefill = currentName,
        waitForPositiveButton = false,
        callback = { _, text -> currentName = text.toString() }
      )
      .positiveButton(android.R.string.ok) { onPositive() }
      .negativeButton(android.R.string.cancel)
  }

  /**
   * Called to save this Controller's state in the event that its host Activity is destroyed.
   *
   * @param outState The Bundle into which data should be saved
   */
  override fun onSaveInstanceState(outState: Bundle) {
    outState.putSerializable(CATEGORY_KEY, categoryId)
    super.onSaveInstanceState(outState)
  }

  /**
   * Restores data that was saved in the [onSaveInstanceState] method.
   *
   * @param savedInstanceState The bundle that has data to be restored
   */
  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    categoryId = savedInstanceState.getLong(CATEGORY_KEY).takeIf { it != -1L }
  }

  /**
   * Called when the positive button of the dialog is clicked.
   */
  private fun onPositive() {
    val target = targetController as? Listener ?: return
    val categoryId = categoryId ?: return

    target.renameCategory(categoryId, currentName)
  }

  interface Listener {
    fun renameCategory(categoryId: Long, name: String)
  }

  private companion object {
    const val CATEGORY_KEY = "CategoryRenameDialog.categoryId"
  }

}
