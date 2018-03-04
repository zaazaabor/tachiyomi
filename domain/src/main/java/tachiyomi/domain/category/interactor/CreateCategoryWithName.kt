package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import tachiyomi.domain.category.exception.CategoryAlreadyExists
import tachiyomi.domain.category.exception.EmptyCategoryName
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryWithName @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(name: String): Completable {
    if (name.isBlank()) {
      return Completable.error(EmptyCategoryName())
    }
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        if (categories.none { name.equals(it.name, ignoreCase = true) }) {
          val nextOrder = categories.maxBy { it.order }?.order?.plus(1) ?: 0
          categoryRepository.createCategory(name, nextOrder)
        } else {
          Completable.error(CategoryAlreadyExists(name))
        }
      }
  }
}
