package tachiyomi.source

abstract class SourceFactory(private val dependencies: Dependencies) {

  abstract fun createSources(): List<Source>

}
