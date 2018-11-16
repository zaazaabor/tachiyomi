package tachiyomi.domain.library.interactor

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import tachiyomi.core.rx.combineLatest
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.model.Library
import tachiyomi.domain.library.model.LibraryCategory
import tachiyomi.domain.library.model.LibraryFilter
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.source.SourceManager
import java.util.Collections
import javax.inject.Inject

class GetLibrary @Inject constructor(
  private val libraryRepository: LibraryRepository,
  private val categoryRepository: CategoryRepository,
  private val sourceManager: SourceManager
) {

  private val filterRelay = BehaviorRelay.createDefault(emptyList<LibraryFilter>())

  private val sortRelay = BehaviorRelay.createDefault<LibrarySort>(LibrarySort.Title(true))

  fun interact(): Flowable<Library> {
    return categoryRepository.getCategories()
      .combineLatest(libraryRepository.getLibraryMangas(), ::toLibrary)
      .combineLatest(filterRelay
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged(), ::applyFilters)
      .combineLatest(sortRelay
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged(), ::applySort)
  }

  private fun toLibrary(categories: List<Category>, mangas: List<LibraryManga>): Library {
    val byCategory = mangas.groupBy { it.category }
    return categories.map { LibraryCategory(it, byCategory[it.id].orEmpty()) }
  }

  private fun applyFilters(library: Library, filters: List<LibraryFilter>): Library {
    if (filters.isEmpty()) return library

    // TODO
    return library
  }

  private fun applySort(library: Library, sort: LibrarySort): Library {
    val sortFn: (LibraryManga, LibraryManga) -> Int = { e1, e2 ->
      when (sort) {
        is LibrarySort.Title -> {
          e1.title.compareTo(e2.title)
        }
        is LibrarySort.LastRead -> {
          TODO()
        }
        is LibrarySort.LastUpdated -> {
          e2.lastUpdate.compareTo(e1.lastUpdate)
        }
        is LibrarySort.Unread -> {
          e1.unread.compareTo(e2.unread)
        }
        is LibrarySort.TotalChapters -> {
          TODO()
        }
        is LibrarySort.Source -> {
          val source1Name = sourceManager.get(e1.source)?.name ?: ""
          val source2Name = sourceManager.get(e2.source)?.name ?: ""
          source1Name.compareTo(source2Name)
        }
      }
    }

    val comparator = if (sort.ascending) Comparator(sortFn) else Collections.reverseOrder(sortFn)

    return library.map { libCategory ->
      LibraryCategory(libCategory.category, libCategory.mangas.sortedWith(comparator))
    }
  }

  fun setSorting(sort: LibrarySort) {
    sortRelay.accept(sort)
  }

  fun setFilters(filters: List<LibraryFilter>) {
    filterRelay.accept(filters)
  }

}
