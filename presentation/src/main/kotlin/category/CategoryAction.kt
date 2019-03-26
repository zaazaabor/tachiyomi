/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.category

import tachiyomi.domain.library.model.Category

sealed class Action {

  data class CategoriesUpdate(val categories: List<Category>) : Action() {
    override fun reduce(state: ViewState) = state.copy(categories = categories)
  }

  data class CreateCategory(val name: String) : Action()

  data class RenameCategory(val categoryId: Long, val newName: String) : Action()

  data class ReorderCategory(val category: Category, val newPosition: Int) : Action()

  data class DeleteCategories(val categoryIds: Set<Long>) : Action()

  data class Error(val error: Throwable? = null) : Action() {
    override fun reduce(state: ViewState) = state.copy(error = error)
  }

  data class ToggleCategorySelection(val category: Category) : Action() {
    override fun reduce(state: ViewState): ViewState {
      val selectedCategories = state.selectedCategories.toMutableSet()
      if (category.id in selectedCategories) {
        selectedCategories -= category.id
      } else {
        selectedCategories += category.id
      }

      // Make sure only existing categories can be selected
      val currCategoryIds = state.categories.asSequence().map { it.id }.toSet()
      selectedCategories.retainAll { it in currCategoryIds }

      return state.copy(selectedCategories = selectedCategories)
    }
  }

  object UnselectCategories : Action() {
    override fun reduce(state: ViewState) = state.copy(selectedCategories = emptySet())
  }

  open fun reduce(state: ViewState) = state

}
