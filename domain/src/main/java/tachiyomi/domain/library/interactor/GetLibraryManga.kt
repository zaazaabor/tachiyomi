package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.library.repository.LibraryRepository
import tachiyomi.domain.manga.model.Manga
import javax.inject.Inject

class GetLibraryManga @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(): Flowable<List<Manga>> {
    return libraryRepository.getLibraryManga()
  }
}
