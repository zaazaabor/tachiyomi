package tachiyomi.domain.manga.interactor

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import tachiyomi.core.util.Optional
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.model.SManga
import tachiyomi.domain.source.model.SMangasPage
import toothpick.testing.ToothPickRule
import javax.inject.Inject

@RunWith(MockitoJUnitRunner::class)
class GetMangaPageFromCatalogueSourceTest {

  @Rule @JvmField
  val toothpickRule = ToothPickRule(this, "GetMangaPageFromCatalogueSource")

  @Mock lateinit var mangaRepository: MangaRepository
  @Mock lateinit var source: CatalogueSource

  @Inject lateinit var getMangaFromSource: GetMangaPageFromCatalogueSource

  @Before
  fun setup() {
    toothpickRule.inject(this)
  }

  @Test
  fun test_interact() {
    // GIVEN
    val sourceManga1 = SManga("url1", "title1")
    val sourceManga2 = SManga("url2", "title2")
    val sourceManga3 = SManga("url3", "title3")
    val sourceMangaPage = SMangasPage(listOf(sourceManga1, sourceManga2, sourceManga3), false)

    val dbManga1 = Manga(1, 1, "url1", "title1")
    val dbManga2 = Manga(2, 1, "url2", "title2")
    val dbManga3 = Manga(3, 1, "url3", "title3")

    `when`(source.id).thenReturn(1)
    `when`(source.fetchMangaList(anyInt())).thenReturn(sourceMangaPage)

    `when`(mangaRepository.getManga(eq("url1"), anyLong()))
      .thenReturn(Flowable.just(Optional.of(dbManga1)))

    `when`(mangaRepository.getManga(eq("url2"), anyLong()))
      .thenReturn(Flowable.just(Optional.None))

    `when`(mangaRepository.getManga(eq("url3"), anyLong()))
      .thenReturn(Flowable.just(Optional.None))

    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga1), anyLong()))
      .thenReturn(Single.just(dbManga1))

    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga2), anyLong()))
      .thenReturn(Single.just(dbManga2))

    `when`(mangaRepository.saveAndReturnNewManga(eq(sourceManga3), anyLong()))
      .thenReturn(Single.just(dbManga3))

    // WHEN
    getMangaFromSource.interact(source, 1).subscribe()

    // THEN
    verify(mangaRepository, never()).saveAndReturnNewManga(eq(sourceManga1), anyLong())

    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga2), anyLong())

    verify(mangaRepository).saveAndReturnNewManga(eq(sourceManga3), anyLong())
  }
}
