package tachiyomi.ui.category

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import tachiyomi.domain.category.Category
import tachiyomi.domain.category.interactor.CreateCategoryWithName
import tachiyomi.domain.category.repository.CategoryRepository
import javax.inject.Inject

data class ViewState(
  val categories: List<Category> = emptyList(),
  val isLoading: Boolean = true,
  val error: Exception? = null
)

class CategoryPresenter @Inject constructor(
  private val categoryRepository: CategoryRepository,
  private val createCategoryUseCase: CreateCategoryWithName
) : MviBasePresenter<CategoryController, ViewState>() {

  private var state = ViewState()

  override fun bindIntents() {
    val modelObservable = categoryRepository.getCategories()
      .observeOn(AndroidSchedulers.mainThread())
      .map { state.copy(categories = it) }
      .doOnNext { state = it }
      .toObservable()

    intent(CategoryController::createCategoryIntent)
      .observeOn(Schedulers.io())
      .flatMapCompletable { createCategoryUseCase.interact(it) }
      .subscribe()

    subscribeViewState(modelObservable, CategoryController::render)
  }

}
