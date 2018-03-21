package tachiyomi.domain.manga.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import tachiyomi.core.util.Optional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.model.SManga

interface MangaRepository {

  fun setFlags(manga: Manga, flags: Int): Completable

  fun getManga(mangaId: Long): Flowable<Optional<Manga>>

  fun getManga(key: String, sourceId: Long): Flowable<Optional<Manga>>

  fun saveAndReturnNewManga(manga: SManga, sourceId: Long): Single<Manga>

  fun deleteNonFavorite(): Completable
}
