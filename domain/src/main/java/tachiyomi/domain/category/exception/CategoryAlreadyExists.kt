package tachiyomi.domain.category.exception

class CategoryAlreadyExists(name: String) : Exception("A category with name $name already exists!")
