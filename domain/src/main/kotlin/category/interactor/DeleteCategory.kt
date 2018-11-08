package tachiyomi.domain.category.interactor

import io.reactivex.Completable
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategory @Inject constructor(
  private val categoryRepository: CategoryRepository
) {

  fun interact(categoryId: Long): Completable {
    return categoryRepository.deleteCategory(categoryId)
      .onErrorComplete()
  }

  fun interact(category: Category): Completable {
    return interact(category.id)
  }


}
