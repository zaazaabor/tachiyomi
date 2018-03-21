package tachiyomi.domain.category.interactor

import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class GetCommonCategories @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(mangas: List<Manga>): Single<List<Category>> {
    return Flowable.fromIterable(mangas)
      .flatMap { categoryRepository.getCategoriesForManga(it.id).take(1) }
      .flatMapIterable { it }
      .distinct { it.id }
      .toList()
  }

}
