package tachiyomi.domain.category.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.domain.category.Category

interface CategoryRepository {

  fun getCategories(): Flowable<List<Category>>

  fun getCategoriesForManga(mangaId: Long): Flowable<List<Category>>

  fun addCategory(category: Category): Completable

  fun addCategories(categories: List<Category>): Completable

  fun createCategory(name: String, order: Int): Completable

  fun renameCategory(categoryId: Long, newName: String): Completable

  fun reorderCategories(categories: List<Category>): Completable

  fun deleteCategory(categoryId: Long): Completable

  fun deleteCategories(categoryIds: List<Long>): Completable

  fun setCategoriesForMangas(categoryIds: List<Long>, mangaIds: List<Long>): Completable
}
