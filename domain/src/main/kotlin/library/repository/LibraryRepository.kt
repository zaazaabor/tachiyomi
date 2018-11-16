package tachiyomi.domain.library.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

interface LibraryRepository {

  fun getLibraryMangas(): Flowable<List<LibraryManga>>

  fun addToLibrary(manga: Manga): Completable

  fun removeFromLibrary(manga: Manga): Completable
}
