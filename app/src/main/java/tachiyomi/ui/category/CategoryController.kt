package tachiyomi.ui.category

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.MviController
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tachiyomi.applicationScope
import toothpick.Toothpick

class CategoryController : MviController<CategoryController, CategoryPresenter>() {

  private val scope = applicationScope(this).apply {
    installModules(CategoryModule())
  }

  private var adapter: CategoryAdapter? = null

  override fun createPresenter(): CategoryPresenter {
    return scope.getInstance(CategoryPresenter::class.java)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    adapter = CategoryAdapter()

    val recycler = RecyclerView(container.context)
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.adapter = adapter
    return recycler
  }

  private val createCategorySubject = PublishSubject.create<String>()

  fun createCategoryIntent(): Observable<String> {
    return createCategorySubject
  }

  fun createCategory(name: String) {
    createCategorySubject.onNext(name)
  }

  fun render(state: ViewState) {
    adapter?.updateItems(state.categories)
  }

  override fun onDestroyView(view: View) {
    adapter = null
    super.onDestroyView(view)
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
  }
}
