package tachiyomi.domain.library.interactor

import io.reactivex.Flowable
import tachiyomi.domain.library.LibraryEntry
import tachiyomi.domain.library.repository.LibraryRepository
import javax.inject.Inject

class GetLibraryEntries @Inject constructor(
  private val libraryRepository: LibraryRepository
) {

  fun interact(): Flowable<List<LibraryEntry>> {
    return libraryRepository.getLibraryEntries()
  }
}
