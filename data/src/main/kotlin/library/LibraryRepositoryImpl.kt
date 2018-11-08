package tachiyomi.data.library

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.Query
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.data.category.table.MangaCategoryTable
import tachiyomi.data.chapter.table.ChapterTable
import tachiyomi.data.library.resolver.LibraryMangaGetResolver
import tachiyomi.data.manga.resolver.MangaFavoritePutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.LibraryEntry
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite,
  private val categoryRepository: CategoryRepository
) : LibraryRepository {

  override fun getLibraryManga(): Flowable<List<Manga>> {
    return storio.get()
      .listOfObjects(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_FAVORITE} = ?")
        .whereArgs(1)
        .orderBy(MangaTable.COL_TITLE)
        .build())
      .prepare()
      .asRxFlowable(BackpressureStrategy.BUFFER)
  }

  override fun getLibraryEntries(): Flowable<List<LibraryEntry>> {
    return storio.get()
      .listOfObjects(LibraryEntry::class.java)
      .withQuery(RawQuery.builder()
        .query(LibraryMangaGetResolver.query)
        .observesTables(MangaTable.TABLE, ChapterTable.TABLE,
          MangaCategoryTable.TABLE, CategoryTable.TABLE)
        .build())
      .withGetResolver(LibraryMangaGetResolver)
      .prepare()
      .asRxFlowable(BackpressureStrategy.BUFFER)
  }

  override fun addToLibrary(manga: Manga): Completable {
    return setFavorite(manga, true)
  }

  override fun removeFromLibrary(manga: Manga): Completable {
    return setFavorite(manga, false)
  }

  private fun setFavorite(manga: Manga, favorite: Boolean): Completable {
    return storio.put()
      .`object`(manga.copy(favorite = favorite))
      .withPutResolver(MangaFavoritePutResolver())
      .prepare()
      .asRxCompletable()
  }
}
