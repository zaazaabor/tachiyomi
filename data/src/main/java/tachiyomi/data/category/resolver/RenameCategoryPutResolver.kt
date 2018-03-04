package tachiyomi.data.category.resolver

import android.content.ContentValues
import tachiyomi.data.category.table.CategoryTable
import tachiyomi.domain.category.Category

internal class RenameCategoryPutResolver : CategoryUpdateResolver() {

  override fun mapToContentValues(category: Category): ContentValues {
    return ContentValues(1).apply {
      put(CategoryTable.COL_ORDER, category.order)
    }
  }
}
