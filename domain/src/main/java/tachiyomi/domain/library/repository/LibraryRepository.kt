package tachiyomi.domain.library.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.domain.library.LibraryEntry
import tachiyomi.domain.manga.model.Manga

interface LibraryRepository {

  fun getLibraryManga(): Flowable<List<Manga>>

  fun getLibraryEntries(): Flowable<List<LibraryEntry>>

  fun addToLibrary(manga: Manga): Completable

  fun removeFromLibrary(manga: Manga): Completable
}
