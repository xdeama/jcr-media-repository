package de.dmalo.mediarepository.exception

class CategoryTypeNotSupportedException(categoryType: String) : RuntimeException() {
    override val message: String = "CategoryType '$categoryType' is not supported."
}
