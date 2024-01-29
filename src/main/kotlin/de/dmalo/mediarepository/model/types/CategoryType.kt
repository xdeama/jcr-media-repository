package de.dmalo.mediarepository.model.types

import de.dmalo.mediarepository.exception.CategoryTypeNotSupportedException


enum class CategoryType(val nodeName: String) {
    IMAGE("image"),
    DOCUMENT("doc"),
    VIDEO("video"),
    UNDEFINED("other");

    override fun toString(): String {
        return nodeName
    }

    companion object {
        fun fromString(value: String): CategoryType {
            val matchingCategoryType = entries.firstOrNull {
                it.nodeName.trim().lowercase() == value.trim().lowercase()
                        || it.name.trim().lowercase() == value.trim().lowercase()
            }
            return matchingCategoryType ?: throw CategoryTypeNotSupportedException(value)
        }
    }
}
