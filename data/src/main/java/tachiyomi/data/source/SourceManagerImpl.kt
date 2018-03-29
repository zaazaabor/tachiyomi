package tachiyomi.data.source

import android.app.Application
import tachiyomi.core.BuildConfig
import tachiyomi.domain.source.CatalogueSource
import tachiyomi.domain.source.Source
import tachiyomi.domain.source.SourceManager
import tachiyomi.domain.source.TestSource
import javax.inject.Inject

class SourceManagerImpl @Inject constructor(
  private val context: Application
) : SourceManager {

  private val sources = mutableMapOf<Long, Source>()

  init {
    if (BuildConfig.DEBUG) {
      registerSource(TestSource()) // TODO temporary
    }
  }

  override fun get(key: Long): Source? {
    return sources[key]
  }

  override fun getSources(): List<CatalogueSource> {
    return sources.values.filterIsInstance<CatalogueSource>()
  }

  override fun registerSource(source: Source, overwrite: Boolean) {
    if (overwrite || !sources.containsKey(source.id)) {
      sources[source.id] = source
    }
  }

  override fun unregisterSource(source: Source) {
    sources.remove(source.id)
  }
}
