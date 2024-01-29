package de.dmalo.mediarepository.model

import de.dmalo.mediarepository.model.types.EncodingType
import de.dmalo.mediarepository.model.types.MimeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.*

class BinaryFileResourceImplTest {

    private val binaryFileResource = BinaryFileResourceImpl(
        data = ByteArrayInputStream("test data".toByteArray()),
        fileName = "test",
        mimeType = MimeType.JPEG,
        binaryEncoding = EncodingType.UTF_8,
        fileSizeInBytes = 9,
        creatorUserName = "jUNIT",
        tags = listOf("unit", "testing")
    )

    @Test
    fun getFileExtension() {
        assertEquals("jpeg", binaryFileResource.getFileExtension())
    }

    @Test
    fun getMimeTypeNodeName() {
        assertEquals("jpeg", binaryFileResource.getMimeTypeNodeName())
    }

    @Test
    fun getCategoryNodeName() {
        assertEquals("image", binaryFileResource.getCategoryNodeName())
    }

    @Test
    fun getMimeTypeString() {
        assertEquals("image/jpeg", binaryFileResource.getMimeTypeTypeString())
    }

    @Test
    fun equalsWithUnequalArrayElementOrder() {
        val date = Calendar.getInstance()

        val firstBinaryFileResource = BinaryFileResourceImpl(
            data = ByteArrayInputStream("test data".toByteArray()),
            fileName = "test",
            mimeType = MimeType.JPEG,
            binaryEncoding = EncodingType.UTF_8,
            fileSizeInBytes = 9,
            createdByUser = "jUNIT",
            lastModifiedByUser = "jUNIT",
            createdDate = date,
            lastModifiedDate = date,
            tags = listOf("unit", "testing", "order", "sorted")
        )

        val secondBinaryFileResource = BinaryFileResourceImpl(
            data = ByteArrayInputStream("test data".toByteArray()),
            fileName = "test",
            mimeType = MimeType.JPEG,
            binaryEncoding = EncodingType.UTF_8,
            fileSizeInBytes = 9,
            createdByUser = "jUNIT",
            lastModifiedByUser = "jUNIT",
            createdDate = date,
            lastModifiedDate = date,
            tags = listOf("testing", "unit", "sorted", "order")
        )

        assertTrue(firstBinaryFileResource == secondBinaryFileResource)
    }
}
