package tachiyomi.data.manga

import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.data.manga.resolver.MangaFlagsPutResolver
import tachiyomi.data.manga.resolver.NewMangaPutResolver
import tachiyomi.data.manga.table.MangaTable
import tachiyomi.data.manga.util.asDbManga
import tachiyomi.data.manga.util.asNewManga
import tachiyomi.domain.manga.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.SManga
import javax.inject.Inject

internal class MangaRepositoryImpl @Inject constructor(
  private val storio: StorIOSQLite
) : MangaRepository {

  fun getManga(mangaId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_ID} = ?")
        .whereArgs(mangaId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun getManga(url: String, sourceId: Long): Maybe<Manga> {
    return storio.get()
      .`object`(Manga::class.java)
      .withQuery(Query.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_URL} = ? AND ${MangaTable.COL_SOURCE} = ?")
        .whereArgs(url, sourceId)
        .build())
      .prepare()
      .asRxMaybe()
  }

  override fun saveAndReturnNewManga(manga: SManga, sourceId: Long): Single<Manga> {
    val newManga = manga.asNewManga(sourceId)
    return storio.put()
      .`object`(newManga)
      .withPutResolver(NewMangaPutResolver())
      .prepare()
      .asRxSingle()
      .map { newManga.asDbManga(it.insertedId()!!) }
  }

  override fun setFlags(manga: Manga, flags: Int): Completable {
    return storio.put()
      .`object`(manga.copy(flags = flags))
      .withPutResolver(MangaFlagsPutResolver())
      .prepare()
      .asRxCompletable()
  }

  override fun deleteNonFavorite(): Completable {
    return storio.delete()
      .byQuery(DeleteQuery.builder()
        .table(MangaTable.TABLE)
        .where("${MangaTable.COL_FAVORITE} = ?")
        .whereArgs(0)
        .build())
      .prepare()
      .asRxCompletable()
  }
}
