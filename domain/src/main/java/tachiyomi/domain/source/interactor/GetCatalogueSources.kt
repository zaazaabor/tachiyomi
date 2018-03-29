package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.SourceManager
import javax.inject.Inject

class GetCatalogueSources @Inject constructor(
  private val sourceManager: SourceManager
) {

  fun interact(): Single<List<CatalogueSource>> {
    return Single.fromCallable { sourceManager.getSources() }
  }
}
