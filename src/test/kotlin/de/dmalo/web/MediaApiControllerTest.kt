package de.dmalo.web

import de.dmalo.mediarepository.exception.MimeTypeNotSupportedException
import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.repository.JcrMediaRepository
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayInputStream
import java.util.*

internal class MediaApiControllerTest {

    private val jcrMediaRepository: JcrMediaRepository = mock()
    private val mediaApiController = MediaApiController(jcrMediaRepository)

    @Test
    fun getResourcesShouldReturnAllResourcesWhenNoParametersAreProvided() {
        val mockFileResourceList = listOf<BinaryFileResource>()

        whenever(jcrMediaRepository.getAll()).thenReturn(mockFileResourceList)

        val response = mediaApiController.getResources(null, null, null, null, mock())

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(jcrMediaRepository).getAll()
    }

    @Test
    fun putResourceShouldCreateResourceAndReturnCreatedStatus() {
        val mockFile = MockMultipartFile("file", "Hello, World!".toByteArray())
        val resourceName = "mockResourceName"
        val mimeType = "text/plain"
        val encoding = "UTF-8"
        val tags = listOf("foo", "bar")
        val createdByUser = "mockUser"

        val response =
            mediaApiController.putResource(mockFile, resourceName, mimeType, encoding, tags, createdByUser, mock())

        assertEquals(HttpStatus.CREATED, response.statusCode)
        verify(jcrMediaRepository).createOrReplace(any())
    }

    @Test
    fun deleteResourceShouldDeleteTheSpecificResourceAndReturnOkStatus() {
        val resourceName = "mockResourceName"
        val mimeType = "image/jpeg"

        val response = mediaApiController.deleteResource(mimeType, resourceName)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(jcrMediaRepository).delete(MimeType.fromString(mimeType), resourceName)
    }

    @Test
    fun getResourceContentShouldReturnResourceContentWithStatusOk() {
        val resourceName = "mockResourceName"
        val mimeType = "image/jpeg"
        val fileContent = "Hello, World!"
        val binaryFileResource = BinaryFileResourceImpl(
            data = ByteArrayInputStream(fileContent.toByteArray()),
            fileName = resourceName,
            mimeType = mimeType,
            binaryEncoding = null,
            fileSizeInBytes = fileContent.toByteArray().size,
            creatorUserName = "mockUser",
            tags = listOf("tag1", "tag2")
        )

        whenever(jcrMediaRepository.get(MimeType.fromString(mimeType), resourceName)).thenReturn(binaryFileResource)

        val response = mediaApiController.getResourceContent(mimeType, resourceName, mock())

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(jcrMediaRepository).get(MimeType.fromString(mimeType), resourceName)
    }

    @Test
    fun handleResourceNotFoundExceptionShouldReturnNotFoundStatus() {
        val ex = ResourceNotFoundException("Resource not found")

        val response = mediaApiController.handleResourceNotFoundException(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun handleBadRequestExceptionShouldReturnBadRequestStatus() {
        val ex = MimeTypeNotSupportedException("MimeType not supported")

        val response = mediaApiController.handleBadRequestException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun handleRuntimeExceptionShouldReturnInternalServerErrorStatus() {
        val ex = RuntimeException("Runtime exception")

        val response = mediaApiController.handleRuntimeException(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun testResourcesHaveCorrectPayloadUrl() {
        val binaryFileResourceMock: BinaryFileResource = mock()
        val resourcesList = listOf(binaryFileResourceMock)
        whenever(binaryFileResourceMock.fileName).thenReturn("Sample_File")
        whenever(binaryFileResourceMock.mimeType).thenReturn(MimeType.XML)
        whenever(binaryFileResourceMock.createdDate).thenReturn(Calendar.getInstance())
        whenever(binaryFileResourceMock.lastModifiedDate).thenReturn(Calendar.getInstance())
        whenever(binaryFileResourceMock.createdByUser).thenReturn("user")
        whenever(binaryFileResourceMock.lastModifiedByUser).thenReturn("user")
        whenever(jcrMediaRepository.getAll()).thenReturn(resourcesList)

        val serverUrl = "http://localhost:8080"
        val expectedPayloadUrl =
            "$serverUrl/api/media/v1/resources/payload?resourceName=Sample_File&mimeType=application/xml"
        val requestMock: HttpServletRequest = mock()
        whenever(requestMock.isSecure).thenReturn(false)
        whenever(requestMock.serverName).thenReturn("localhost")
        whenever(requestMock.serverPort).thenReturn(8080)

        val responseDtoWrapper = mediaApiController.getResources(null, null, null, null, requestMock).body

        val actualPayloadUrl = responseDtoWrapper?.mediaResourceDTOList?.first()?.payloadURI
        assertEquals(expectedPayloadUrl, actualPayloadUrl)
    }
}
