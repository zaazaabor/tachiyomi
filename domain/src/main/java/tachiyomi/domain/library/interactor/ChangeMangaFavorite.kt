package tachiyomi.domain.library.interactor

import io.reactivex.Completable
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.Manga
import javax.inject.Inject

class ChangeMangaFavorite @Inject constructor(private val libraryRepository: LibraryRepository) {

  fun interact(manga: Manga): Completable {
    return if (manga.favorite) {
      libraryRepository.removeFromLibrary(manga)
    } else {
      libraryRepository.addToLibrary(manga)
    }
  }
}
