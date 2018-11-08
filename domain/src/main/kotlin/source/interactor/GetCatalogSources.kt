package tachiyomi.domain.source.interactor

import io.reactivex.Single
import tachiyomi.domain.source.SourceManager
import tachiyomi.source.CatalogSource
import javax.inject.Inject

class GetCatalogSources @Inject constructor(
  private val sourceManager: SourceManager
) {

  fun interact(): Single<List<CatalogSource>> {
    return Single.fromCallable { sourceManager.getSources() }
  }
}
