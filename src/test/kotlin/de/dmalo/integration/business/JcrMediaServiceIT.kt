package de.dmalo.integration.business

import de.dmalo.assertions.softAssert
import de.dmalo.business.JcrMediaService
import de.dmalo.integration.testcontext.IntegrationTestConfiguration
import de.dmalo.mediarepository.exception.ResourceAlreadyExistsException
import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.repository.JcrMediaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream
import java.text.SimpleDateFormat
import javax.jcr.Repository
import javax.jcr.Session
import javax.jcr.SimpleCredentials

@IntegrationTestConfiguration
class JcrMediaServiceIT {

    @Autowired
    private lateinit var repository: Repository

    @Autowired
    private lateinit var jcrMediaRepository: JcrMediaRepository

    private lateinit var jcrMediaService: JcrMediaService

    @BeforeEach
    fun setUp() {
        jcrMediaService = JcrMediaService(jcrMediaRepository)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    private fun removeResourceIfExists(path: String) {
        val session: Session = repository.login(SimpleCredentials("admin", "itpassword".toCharArray()))
        try {
            try {
                session.getNode(path).remove()
            } catch (_: Exception) {
            }
            session.save()
        } finally {
            session.logout()
        }
    }


    @Test
    fun `test repository initialization`() {
        val categoryNodes = listOf("image", "doc", "video", "other")

        val session: Session = repository.login(SimpleCredentials("admin", "itpassword".toCharArray()))
        try {
            val mediaNode = session.rootNode.getNode("media")

            softAssert {
                for (nodeName in categoryNodes) {
                    assertTrue(mediaNode.hasNode(nodeName), "Node for category $nodeName does not exist")
                }

                for (mimeType in MimeType.entries) {
                    val categoryNode = mediaNode.getNode(mimeType.category.nodeName)
                    assertTrue(
                        categoryNode.hasNode(mimeType.fileExtension),
                        "Node for mimeType $mimeType.fileExtension does not exist"
                    )
                }
            }
        } finally {
            session.logout()
        }
    }

    @Test
    fun `test saving and retrieving image`() {
        val fileName = "testImage1"
        val testData = getTestDataFromFile()
        val binaryFileResource = createImageFileResource(fileName, testData)

        jcrMediaService.create(binaryFileResource)

        val retrievedResource: BinaryFileResource = jcrMediaService.get(MimeType.JPEG, fileName)

        softAssert {
            assertEquals(binaryFileResource.fileName, retrievedResource.fileName)
            assertEquals(binaryFileResource.mimeType, retrievedResource.mimeType)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.fileSizeInBytes, retrievedResource.fileSizeInBytes)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.createdByUser, retrievedResource.createdByUser)
            assertEquals(
                dateFormat.format(binaryFileResource.createdDate.time),
                dateFormat.format(retrievedResource.createdDate.time)
            )
            assertEquals(
                dateFormat.format(binaryFileResource.lastModifiedDate.time),
                dateFormat.format(retrievedResource.lastModifiedDate.time)
            )
            assertEquals(binaryFileResource.lastModifiedByUser, retrievedResource.lastModifiedByUser)
            assertTrue(binaryFileResource.tags == retrievedResource.tags)

            val retrievedByteArray = retrievedResource.data.readAllBytes()
            assertEquals(retrievedByteArray.size, testData.size)
            assertTrue(retrievedByteArray contentEquals testData)
        }

        removeResourceIfExists("/media/image/jpeg/testImage1")
    }

    @Test
    fun `test saving and retrieving image with getAll`() {
        val testData = getTestDataFromFile()
        val binaryFileResource = createImageFileResource("testImage1", testData)

        jcrMediaService.create(binaryFileResource)

        val retrievedResourceList = jcrMediaService.getAll()

        softAssert {
            val retrievedResource = retrievedResourceList[0]
            assertEquals(binaryFileResource.fileName, retrievedResource.fileName)
            assertEquals(binaryFileResource.mimeType, retrievedResource.mimeType)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.fileSizeInBytes, retrievedResource.fileSizeInBytes)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.createdByUser, retrievedResource.createdByUser)
            assertEquals(
                dateFormat.format(binaryFileResource.createdDate.time),
                dateFormat.format(retrievedResource.createdDate.time)
            )
            assertEquals(
                dateFormat.format(binaryFileResource.lastModifiedDate.time),
                dateFormat.format(retrievedResource.lastModifiedDate.time)
            )
            assertEquals(binaryFileResource.lastModifiedByUser, retrievedResource.lastModifiedByUser)
            assertTrue(binaryFileResource.tags == retrievedResource.tags)

            val retrievedByteArray = retrievedResource.data.readAllBytes()
            assertEquals(retrievedByteArray.size, testData.size)
            assertTrue(retrievedByteArray contentEquals testData)
        }

        removeResourceIfExists("/media/image/jpeg/testImage1")
    }

    @Test
    fun `test getAll with empty repository`() {
        val resultList = jcrMediaService.getAll()
        assertTrue(resultList.isEmpty())
    }

    @Test
    fun `test saving an image twice`() {
        val testData = getTestDataFromFile()
        val binaryFileResource = createImageFileResource("testImage1", testData)

        jcrMediaService.create(binaryFileResource)
        val thrownException = assertThrows<ResourceAlreadyExistsException> {
            jcrMediaService.create(binaryFileResource)
        }
        assertEquals(
            thrownException.message, "Saving new resource failed, resource at " +
                    "'/media/image/jpeg/testImage1' already exists"
        )

        val retrievedResource = jcrMediaService.get(MimeType.JPEG, binaryFileResource.fileName)

        softAssert {
            assertEquals(binaryFileResource.fileName, retrievedResource.fileName)
            assertEquals(binaryFileResource.mimeType, retrievedResource.mimeType)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.fileSizeInBytes, retrievedResource.fileSizeInBytes)
            assertEquals(binaryFileResource.binaryEncoding, retrievedResource.binaryEncoding)
            assertEquals(binaryFileResource.createdByUser, retrievedResource.createdByUser)
            assertEquals(binaryFileResource.lastModifiedByUser, retrievedResource.lastModifiedByUser)
            assertEquals(
                dateFormat.format(binaryFileResource.createdDate.time),
                dateFormat.format(retrievedResource.createdDate.time)
            )
            assertEquals(
                dateFormat.format(binaryFileResource.lastModifiedDate.time),
                dateFormat.format(retrievedResource.lastModifiedDate.time)
            )
            assertTrue(binaryFileResource.tags == retrievedResource.tags)

            val retrievedByteArray = retrievedResource.data.readAllBytes()
            assertEquals(retrievedByteArray.size, testData.size)
            assertTrue(retrievedByteArray contentEquals testData)
        }

        removeResourceIfExists("/media/image/jpeg/testImage1")
    }

    @Test
    fun `test listSavedFileResources`() {
        val imageResource = createImageFileResource("testImage1", getTestDataFromFile())
        removeResourceIfExists("/media/image/jpeg/${imageResource.fileName}")
        jcrMediaService.create(imageResource)

        val listResources = jcrMediaService.listAllFilePaths()
        assertTrue(listResources.isNotEmpty())
        assertEquals("/media/image/jpeg/testImage1", listResources[0])

        removeResourceIfExists("/media/image/jpeg/testImage1")
    }

    @Test
    fun `test searching by tag`() {
        val imageResource = createImageFileResource("testImage1", getTestDataFromFile())
        jcrMediaService.create(imageResource)

        jcrMediaService.create(
            BinaryFileResourceImpl(
                data = ByteArray(0).inputStream(),
                fileName = "file1",
                mimeType = MimeType.JPEG,
                fileSizeInBytes = 0,
                creatorUserName = "jUNIT-Test",
                tags = listOf("orange", "apple juice")
            )
        )

        jcrMediaService.create(
            BinaryFileResourceImpl(
                data = ByteArray(0).inputStream(),
                fileName = "file2",
                mimeType = MimeType.JPEG,
                fileSizeInBytes = 0,
                creatorUserName = "jUNIT-Test",
                tags = listOf("orange", "pizza")
            )
        )

        jcrMediaService.create(
            BinaryFileResourceImpl(
                data = ByteArray(0).inputStream(),
                fileName = "file3",
                mimeType = MimeType.JPEG,
                fileSizeInBytes = 0,
                creatorUserName = "jUNIT-Test",
                tags = listOf("pizza")
            )
        )

        val listResources = jcrMediaService.getByTag("apple")

        softAssert {
            assertEquals(listResources.size, 1)
            val retrievedResource = listResources[0]
            assertEquals("testImage1", retrievedResource.fileName)

            removeResourceIfExists("/media/image/jpeg/testImage1")
            removeResourceIfExists("/media/image/jpeg/file1")
            removeResourceIfExists("/media/image/jpeg/file2")
            removeResourceIfExists("/media/image/jpeg/file3")
        }
    }

    private fun createImageFileResource(fileName: String, testData: ByteArray): BinaryFileResource {
        return BinaryFileResourceImpl(
            data = testData.inputStream(),
            fileName = fileName,
            mimeType = MimeType.JPEG,
            fileSizeInBytes = getTestDataFromFile().size,
            creatorUserName = "jUNIT-Test",
            tags = listOf("apple", "orange", "apple juice")
        )
    }

    private fun getTestDataFromFile(): ByteArray {
        val imageContentStream: InputStream? =
            this::class.java.classLoader.getResourceAsStream("testdata/image/testimage1.jpg")
        return imageContentStream?.readAllBytes() ?: throw IllegalArgumentException("test file resource not found")
    }
}
