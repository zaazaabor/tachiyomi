package tachiyomi.domain.library.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import tachiyomi.domain.library.LibraryCategory
import tachiyomi.domain.library.LibraryEntry
import tachiyomi.domain.manga.Manga

interface LibraryRepository {

  fun getLibraryManga(): Flowable<List<Manga>>

  fun getLibraryEntries(): Flowable<List<LibraryEntry>>

  fun getLibraryByCategory(): Flowable<List<LibraryCategory>>

  fun addToLibrary(manga: Manga): Completable

  fun removeFromLibrary(manga: Manga): Completable
}
