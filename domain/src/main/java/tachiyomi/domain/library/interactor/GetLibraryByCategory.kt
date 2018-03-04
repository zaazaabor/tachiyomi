package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.library.LibraryCategory
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryByCategory @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(): Flowable<List<LibraryCategory>> {
    return libraryRepository.getLibraryByCategory()
  }
}
