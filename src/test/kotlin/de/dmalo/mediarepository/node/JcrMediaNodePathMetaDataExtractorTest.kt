package de.dmalo.mediarepository.node

import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.model.types.CategoryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class JcrMediaNodePathMetaDataExtractorTest {

    @Test
    fun shouldReturnCorrectCategoryWhenNodePathIsValid() {
        val nodePath = "/media/image"
        val category = JcrMediaNodePathMetaDataExtractor.getCategory(nodePath)
        assertEquals(CategoryType.IMAGE, category)
    }

    @Test
    fun shouldThrowResourceNotFoundExceptionWhenNodePathSizeIsLessThanTwo() {
        val nodePath = "/media"
        assertThrows(ResourceNotFoundException::class.java) {
            JcrMediaNodePathMetaDataExtractor.getCategory(nodePath)
        }
    }
}
