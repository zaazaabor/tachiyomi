package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.exception.CategoryAlreadyExists
import tachiyomi.domain.category.exception.EmptyCategoryName
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class RenameCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long, newName: String): Completable {
    if (newName.isBlank()) {
      return Completable.error(EmptyCategoryName())
    }
    return categoryRepository.getCategories()
      .take(1)
      .flatMapCompletable { categories ->
        if (categories.none { newName.equals(it.name, ignoreCase = true) }) {
          categoryRepository.renameCategory(categoryId, newName)
        } else {
          Completable.error(CategoryAlreadyExists(newName))
        }
      }
  }

  fun interact(category: Category, newName: String): Completable {
    return interact(category.id, newName)
  }
}
