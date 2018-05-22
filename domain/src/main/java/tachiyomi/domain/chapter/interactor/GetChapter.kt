package tachiyomi.domain.chapter.interactor

import io.reactivex.Maybe
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import javax.inject.Inject

class GetChapter @Inject constructor(
  private val repository: ChapterRepository
) {

  fun interact(id: Long): Maybe<Chapter> {
    return repository.getChapter(id)
      .onErrorComplete()
  }

  fun interact(key: String, sourceId: Long): Maybe<Chapter> {
    return repository.getChapter(key, sourceId)
      .onErrorComplete()
  }

}
