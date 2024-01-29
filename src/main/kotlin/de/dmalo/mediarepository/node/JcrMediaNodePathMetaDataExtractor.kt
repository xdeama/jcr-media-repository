package de.dmalo.mediarepository.node

import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.model.types.CategoryType


internal class JcrMediaNodePathMetaDataExtractor {
    companion object {
        fun getCategory(nodePath: String?): CategoryType {
            val parts = nodePath?.split("/") ?: listOf()
            return if (parts.size > 2) CategoryType.fromString(parts[2]) else
                throw ResourceNotFoundException("Unable to extract category from path because of malformed repository")
        }
    }
}
