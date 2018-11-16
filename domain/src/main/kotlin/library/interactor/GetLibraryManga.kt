package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryManga @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(): Flowable<List<LibraryManga>> {
    return libraryRepository.getLibraryMangas()
  }
}
