package tachiyomi.source

abstract class SourceFactory(private val component: Component) {

  abstract fun createSources(): List<Source>

}
