package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.LibraryCategory
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryByCategory @Inject constructor(
  private val libraryRepository: LibraryRepository,
  private val categoryRepository: CategoryRepository
) {

  fun interact(): Flowable<List<LibraryCategory>> {
    return Flowable.combineLatest(
      categoryRepository.getCategories(),
      libraryRepository.getLibraryEntries(),
      BiFunction { categories, library ->
        val byCategory = library.groupBy { it.category }
        categories.map { LibraryCategory(it, byCategory[it.id].orEmpty()) }
      })
  }
}
