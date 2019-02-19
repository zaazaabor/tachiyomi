/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.category

import tachiyomi.domain.category.Category

sealed class Action {

  data class CategoriesUpdate(val categories: List<Category>) : Action() {
    override fun reduce(state: ViewState) = state.copy(categories = categories)
  }

  data class CreateCategory(val name: String) : Action()

  data class DeleteCategory(val category: Category) : Action()

  data class RenameCategory(val category: Category, val newName: String) : Action()

  data class ReorderCategory(val category: Category, val newPosition: Int) : Action()

  data class Error(val error: Throwable? = null) : Action() {
    override fun reduce(state: ViewState) = state.copy(error = error)
  }

  open fun reduce(state: ViewState) = state

}
