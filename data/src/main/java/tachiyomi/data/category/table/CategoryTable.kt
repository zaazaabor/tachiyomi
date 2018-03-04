package tachiyomi.data.category.table

internal object CategoryTable {

  const val TABLE = "categories"

  const val COL_ID = "ca_id"
  const val COL_NAME = "ca_name"
  const val COL_ORDER = "ca_sort"
  const val COL_FLAGS = "ca_flags"

  val createTableQuery: String
    get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER NOT NULL PRIMARY KEY,
            $COL_NAME TEXT NOT NULL,
            $COL_ORDER INTEGER NOT NULL,
            $COL_FLAGS INTEGER NOT NULL
            )"""

}
