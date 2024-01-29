package de.dmalo.mediarepository.mapper

import de.dmalo.assertions.softAssert
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.*
import de.dmalo.mediarepository.springconfig.FileStoreConfig
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import de.dmalo.mediarepository.springconfig.OakConfig
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import javax.jcr.*

@ExtendWith(MockitoExtension::class)
class FileResourceMapperImplTest {

    private lateinit var fileResourceMapper: FileResourceMapperImpl

    @Mock
    private lateinit var config: MediaRepositoryContextProperties

    @BeforeEach
    fun setUp() {
        fileResourceMapper = FileResourceMapperImpl(config)
    }

    @Test
    fun copyJcrNodesToFileResourceCallWithValidDataReturnsValidBinaryFileResource() {
        val date = Calendar.getInstance()
        date.set(2016, 2, 15)
        val testData = ByteArrayInputStream("test data".toByteArray())

        val fileNode = Mockito.mock(Node::class.java)
        val resourceNode = Mockito.mock(Node::class.java)
        val session = Mockito.mock(Session::class.java)
        val binary = Mockito.mock(Binary::class.java)

        `when`(fileNode.session).thenReturn(session)
        `when`(session.isLive).thenReturn(true)

        `when`(fileNode.getNode(anyString())).thenReturn(resourceNode)
        `when`(fileNode.isNodeType(eq(JcrNodeType.MEDIA_FILE.key))).thenReturn(true)
        `when`(fileNode.name).thenReturn("test")

        `when`(resourceNode.isNodeType(eq(JcrNodeType.MEDIA_RESOURCE.key))).thenReturn(true)

        val nodePropertyCreator = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.CREATED_BY.key))).thenReturn(nodePropertyCreator)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_LAST_MODIFIED_BY.key))).thenReturn(nodePropertyCreator)
        `when`(nodePropertyCreator.string).thenReturn("jUNIT")

        val nodePropertyCreatedDate = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_CREATED.key))).thenReturn(nodePropertyCreatedDate)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_LAST_MODIFIED.key))).thenReturn(nodePropertyCreatedDate)
        `when`(nodePropertyCreatedDate.date).thenReturn(date)

        val nodePropertyBinary = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_DATA.key))).thenReturn(nodePropertyBinary)
        `when`(nodePropertyBinary.binary).thenReturn(binary)
        `when`(binary.stream).thenReturn(testData)

        val nodePropertyFileSize = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.FILE_SIZE.key))).thenReturn(nodePropertyFileSize)
        `when`(nodePropertyFileSize.long).thenReturn(9L)

        val nodePropertyEncoding = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_ENCODING.key))).thenReturn(nodePropertyEncoding)
        `when`(nodePropertyEncoding.string).thenReturn("UTF-8")

        val nodePropertyMimetype = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.JCR_MIMETYPE.key))).thenReturn(nodePropertyMimetype)
        `when`(nodePropertyMimetype.string).thenReturn("image/jpeg")

        val nodePropertyTags = Mockito.mock(Property::class.java)
        `when`(resourceNode.getProperty(eq(JcrNodeProperty.TAGS.key))).thenReturn(nodePropertyTags)
        val value = Mockito.mock(Value::class.java)
        val values = arrayOf(value)
        `when`(nodePropertyTags.values).thenReturn(values)
        `when`(value.string).thenReturn("tag")

        val resultResource = fileResourceMapper.copyJcrNodesToFileResource(fileNode)

        softAssert {
            assertEquals("test", resultResource.fileName)
            assertEquals(9, resultResource.fileSizeInBytes)
            assertEquals("jUNIT", resultResource.createdByUser)
            assertEquals("tag", resultResource.tags[0])
            assertEquals("jUNIT", resultResource.lastModifiedByUser)
            assertEquals(date, resultResource.createdDate)
            assertEquals(date, resultResource.lastModifiedDate)
            assertEquals("UTF-8", resultResource.binaryEncoding?.typeString)
            assertArrayEquals("test data".toByteArray(), resultResource.data.readAllBytes())
            assertEquals("image/jpeg", resultResource.getMimeTypeTypeString())
        }
    }

    @Test
    fun copyFileResourceToJcrNodesCallWithValidDataSetsCorrectProperties() {
        val testData = ByteArrayInputStream("test data".toByteArray())

        val binaryFileResource = BinaryFileResourceImpl(
            data = testData,
            fileName = "test",
            mimeType = MimeType.JPEG,
            binaryEncoding = EncodingType.UTF_8,
            fileSizeInBytes = testData.readAllBytes().size,
            creatorUserName = "jUNIT",
            tags = listOf("unit", "testing")
        )

        val parentNode = Mockito.mock(Node::class.java)
        val fileNode = Mockito.mock(Node::class.java)
        val resourceNode = Mockito.mock(Node::class.java)
        val session = Mockito.mock(Session::class.java)
        val valueFactory = Mockito.mock(ValueFactory::class.java)
        val binary = Mockito.mock(Binary::class.java)
        val oakConfig = Mockito.mock(OakConfig::class.java)
        val fileStoreConfig = Mockito.mock(FileStoreConfig::class.java)

        `when`(parentNode.addNode(eq("test"), eq(JcrNodeType.MEDIA_FILE.key))).thenReturn(fileNode)
        `when`(fileNode.addNode(eq(JcrNodeNames.JCR_CONTENT.key), eq(JcrNodeType.MEDIA_RESOURCE.key))).thenReturn(
            resourceNode
        )
        `when`(resourceNode.session).thenReturn(session)
        `when`(session.valueFactory).thenReturn(valueFactory)
        `when`(valueFactory.createBinary(any(InputStream::class.java))).thenReturn(binary)
        `when`(config.oak).thenReturn(oakConfig)
        `when`(oakConfig.fileStore).thenReturn(fileStoreConfig)
        `when`(fileStoreConfig.maxFileSize).thenReturn(100000000)

        fileResourceMapper.copyFileResourceToJcrNodes(binaryFileResource, parentNode)

        softAssert {
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_DATA.key), any(Binary::class.java))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_ENCODING.key), eq("UTF-8"))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_MIMETYPE.key), eq("image/jpeg"))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.FILE_SIZE.key), eq(9L))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_LAST_MODIFIED.key), any(Calendar::class.java))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_LAST_MODIFIED_BY.key), eq("jUNIT"))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.JCR_CREATED.key), any(Calendar::class.java))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.CREATED_BY.key), eq("jUNIT"))
            verify(resourceNode).setProperty(eq(JcrNodeProperty.TAGS.key), eq(listOf("unit", "testing").toTypedArray()))
        }
    }
}
