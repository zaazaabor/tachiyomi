package tachiyomi.domain.manga.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.domain.manga.Manga
import tachiyomi.domain.source.SManga

interface MangaRepository {

  fun setFlags(manga: Manga, flags: Int): Completable

  fun getManga(url: String, sourceId: Long): Maybe<Manga>

  fun saveAndReturnNewManga(manga: SManga, sourceId: Long): Single<Manga>

  fun deleteNonFavorite(): Completable
}
