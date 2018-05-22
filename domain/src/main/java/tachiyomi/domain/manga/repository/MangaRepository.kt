package tachiyomi.domain.manga.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import tachiyomi.core.rx.RxOptional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.model.MangaMeta

interface MangaRepository {

  fun setFlags(manga: Manga, flags: Int): Completable

  fun subscribeManga(mangaId: Long): Flowable<RxOptional<Manga>>

  fun subscribeManga(key: String, sourceId: Long): Flowable<RxOptional<Manga>>

  fun getManga(mangaId: Long): Maybe<Manga>

  fun getManga(key: String, sourceId: Long): Maybe<Manga>

  fun updateMangaDetails(manga: Manga): Completable

  fun saveAndReturnNewManga(manga: MangaMeta, sourceId: Long): Single<Manga>

  fun deleteNonFavorite(): Completable
}
