/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.ui.screens.library

import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort

sealed class Action {

  data class SetFilters(val filters: List<LibraryFilter>) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(filters = filters)
  }

  data class SetSorting(val sort: LibrarySort) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(sort = sort)
  }

  data class LibraryUpdate(val library: List<LibraryManga>) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(library = library)
  }

  data class CategoriesUpdate(val categories: List<Category>) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(
        categories = categories,
        selectedCategoryId = state.selectedCategoryId?.let { selectedId ->
          if (categories.any { it.id == selectedId }) {
            selectedId
          } else {
            categories.firstOrNull()?.id
          }
        }
      )
  }

  data class SetSelectedCategory(val category: Category?) : Action() {
    override fun reduce(state: ViewState): ViewState {
      val selectedCategory = state.categories.find { it.id == category?.id }
      return state.copy(selectedCategoryId = selectedCategory?.id)
    }
  }

  data class ToggleSelection(val manga: LibraryManga) : Action() {
    override fun reduce(state: ViewState) =
      state.copy(selectedManga = if (manga.mangaId in state.selectedManga) {
        state.selectedManga - manga.mangaId
      } else {
        state.selectedManga + manga.mangaId
      })
  }

  object UnselectMangas : Action() {
    override fun reduce(state: ViewState) =
      state.copy(selectedManga = emptySet())
  }

  open fun reduce(state: ViewState) = state
}
