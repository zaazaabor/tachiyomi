package tachiyomi.domain.manga.interactor

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import tachiyomi.core.rx.RxOptional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.model.SManga
import toothpick.testing.ToothPickRule
import javax.inject.Inject

@RunWith(MockitoJUnitRunner::class)
class GetOrAddMangaFromSourceTest {

  @Rule @JvmField
  val toothpickRule = ToothPickRule(this, "GetOrAddMangaFromSource")

  @Mock lateinit var mangaRepository: MangaRepository

  @Inject lateinit var getOrAddMangaFromSource: GetOrAddMangaFromSource

  private val sourceManga = SManga("url", "title")
  private val sourceId = 1L
  private val dbManga = Manga(1, sourceId, sourceManga.key, sourceManga.title)

  @Before
  fun setup() {
    toothpickRule.inject(this)
  }

  @Test
  fun `creates manga when not found in repository`() {
    `when`(mangaRepository.getManga(sourceManga.key, sourceId))
      .thenReturn(Flowable.just(RxOptional.None))

    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga), anyLong()))
      .thenReturn(Single.just(dbManga))

    getOrAddMangaFromSource.interact(sourceManga, sourceId).blockingGet()

    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga), eq(sourceId))
  }

  @Test
  fun `returns manga when found in repository`() {
    `when`(mangaRepository.getManga(sourceManga.key, sourceId))
      .thenReturn(Flowable.just(RxOptional.of(dbManga)))

    getOrAddMangaFromSource.interact(sourceManga, sourceId).blockingGet()

    verify(mangaRepository, never()).saveAndReturnNewManga(sourceManga, sourceId)
  }

}
