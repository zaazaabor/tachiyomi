///*
// * Copyright (C) 2018 The Tachiyomi Open Source Project
// *
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at http://mozilla.org/MPL/2.0/.
// */
//
//package tachiyomi.domain.library.interactor
//
//import com.jakewharton.rxrelay2.BehaviorRelay
//import io.reactivex.BackpressureStrategy
//import io.reactivex.Flowable
//import tachiyomi.core.rx.combineLatest
//import tachiyomi.domain.library.Category
//import tachiyomi.domain.library.repository.CategoryRepository
//import tachiyomi.domain.library.model.Library
//import tachiyomi.domain.library.model.LibraryCategory
//import tachiyomi.domain.library.model.LibraryFilter
//import tachiyomi.domain.library.model.LibraryManga
//import tachiyomi.domain.library.model.LibrarySort
//import tachiyomi.domain.library.repository.LibraryRepository
//import tachiyomi.domain.source.SourceManager
//import java.util.Collections
//import javax.inject.Inject
//
//class GetLibrary @Inject constructor(
//  private val libraryRepository: LibraryRepository,
//  private val categoryRepository: CategoryRepository,
//  private val sourceManager: SourceManager
//) {
//
//  // TODO probably unneeded
//  private val filterRelay = BehaviorRelay.createDefault(emptyList<LibraryFilter>())
//
//  private val sortRelay = BehaviorRelay.createDefault<LibrarySort>(LibrarySort.Title(true))
//
//  fun interact(filters: List<LibraryFilter>): Flowable<Library> {
//    return categoryRepository.getCategories()
//      .combineLatest(libraryRepository.subscribe()) { categories, mangas ->
//        toLibrary(categories, mangas, filters)
//      }
//      .combineLatest(filterRelay
//        .toFlowable(BackpressureStrategy.LATEST)
//        .distinctUntilChanged(), ::applyFilters)
//      .combineLatest(sortRelay
//        .toFlowable(BackpressureStrategy.LATEST)
//        .distinctUntilChanged(), ::applySort)
//  }
//
//  private fun toLibrary(
//    categories: List<Category>,
//    mangas: List<LibraryManga>,
//    filters: List<LibraryFilter>
//  ): Library {
//    val library = mutableListOf<LibraryCategory>()
//    val byCategory = mangas.groupBy { it.category }
//
//    for (category in categories) {
//      val libCat = when (category.id) {
//        Category.ALL_ID -> {
//          if (LibraryFilter.AllCategory !in filters) {
//            val allManga = mangas.distinctBy { it.mangaId }
//            LibraryCategory(category, allManga)
//          } else {
//            null
//          }
//        }
//        Category.UNCATEGORIZED_ID -> {
//          val uncategorizedManga = byCategory[0]
//          if (uncategorizedManga != null && uncategorizedManga.isNotEmpty()
//            && categories.any { !it.isSystemCategory }
//          ) {
//            LibraryCategory(category, uncategorizedManga)
//          } else {
//            null
//          }
//        }
//        else -> LibraryCategory(category, byCategory[category.id].orEmpty())
//      }
//      if (libCat != null) {
//        library.add(libCat)
//      }
//    }
//
//    return library
//  }
//
//  private fun applyFilters(library: Library, filters: List<LibraryFilter>): Library {
//    if (filters.isEmpty()) return library
//
//    // TODO
//    return library
//  }
//
//  private fun applySort(library: Library, sort: LibrarySort): Library {
//    val sortFn: (LibraryManga, LibraryManga) -> Int = { e1, e2 ->
//      when (sort) {
//        is LibrarySort.Title -> {
//          e1.title.compareTo(e2.title)
//        }
//        is LibrarySort.LastRead -> {
//          TODO()
//        }
//        is LibrarySort.LastUpdated -> {
//          e2.lastUpdate.compareTo(e1.lastUpdate)
//        }
//        is LibrarySort.Unread -> {
//          e1.unread.compareTo(e2.unread)
//        }
//        is LibrarySort.TotalChapters -> {
//          TODO()
//        }
//        is LibrarySort.Source -> {
//          val source1Name = sourceManager.get(e1.source)?.name ?: ""
//          val source2Name = sourceManager.get(e2.source)?.name ?: ""
//          source1Name.compareTo(source2Name)
//        }
//      }
//    }
//
//    val comparator = if (sort.ascending) Comparator(sortFn) else Collections.reverseOrder(sortFn)
//
//    return library.map { libCategory ->
//      LibraryCategory(libCategory.category, libCategory.mangas.sortedWith(comparator))
//    }
//  }
//
//  fun setSorting(sort: LibrarySort) {
//    sortRelay.accept(sort)
//  }
//
//  fun setFilters(filters: List<LibraryFilter>) {
//    filterRelay.accept(filters)
//  }
//
//}
