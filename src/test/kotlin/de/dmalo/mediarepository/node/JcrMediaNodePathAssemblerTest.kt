package de.dmalo.mediarepository.node

import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.EncodingType
import de.dmalo.mediarepository.model.types.MimeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class JcrMediaNodePathAssemblerTest {
    private val binaryFileResource: BinaryFileResource = BinaryFileResourceImpl(
        data = ByteArrayInputStream("test data".toByteArray()),
        fileName = "test",
        mimeType = MimeType.JPEG,
        binaryEncoding = EncodingType.UTF_8,
        fileSizeInBytes = 9,
        creatorUserName = "jUNIT",
        tags = listOf("unit", "testing")
    )


    @Test
    fun testGetResourceNodePathFromBinaryFileResource() {
        val typePath = JcrMediaNodePathAssembler.getResourceNodePath(binaryFileResource)
        assertEquals("/media/image/jpeg/test/jcr:content", typePath)
    }

    @Test
    fun testGetResourceNodePathFromMimeType() {
        val typePath = JcrMediaNodePathAssembler.getResourceNodePath(MimeType.JPEG, "test")
        assertEquals("/media/image/jpeg/test/jcr:content", typePath)
    }

    @Test
    fun testGetResourceNodePath() {
        val typePath = JcrMediaNodePathAssembler.getResourceNodePath("image", "jpeg", "test")
        assertEquals("/media/image/jpeg/test/jcr:content", typePath)
    }

    @Test
    fun testGetFileNodePathFromBinaryFileResource() {
        val typePath = JcrMediaNodePathAssembler.getFileNodePath(binaryFileResource)
        assertEquals("/media/image/jpeg/test", typePath)
    }

    @Test
    fun testGetFileNodePathFromMimeType() {
        val typePath = JcrMediaNodePathAssembler.getFileNodePath(MimeType.JPEG, "test")
        assertEquals("/media/image/jpeg/test", typePath)
    }

    @Test
    fun testGetFileNodePath() {
        val typePath = JcrMediaNodePathAssembler.getFileNodePath("image", "jpeg", "test")
        assertEquals("/media/image/jpeg/test", typePath)
    }

    @Test
    fun testGetTypePathFromBinaryFileResource() {
        val typePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)
        assertEquals("/media/image/jpeg", typePath)
    }

    @Test
    fun testGetTypePath() {
        val typePath = JcrMediaNodePathAssembler.getTypePath("image", "jpeg")
        assertEquals("/media/image/jpeg", typePath)
    }

    @Test
    fun testGetCategoryPathFromBinaryFileResource() {
        val typePath = JcrMediaNodePathAssembler.getCategoryPath(binaryFileResource)
        assertEquals("/media/image", typePath)
    }

    @Test
    fun testGetCategoryPath() {
        val typePath = JcrMediaNodePathAssembler.getCategoryPath("image")
        assertEquals("/media/image", typePath)
    }


}
