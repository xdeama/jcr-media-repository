package de.dmalo.integration.web.api

import com.fasterxml.jackson.databind.ObjectMapper
import de.dmalo.integration.testcontext.IntegrationTestConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.InputStream

@IntegrationTestConfiguration
internal class MediaApiControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser
    fun testGetResourceAndPayload() {

        // Prepare resource create, download and delete
        val resourceName = "testimage1.jpg"
        val mimeTypeValue = "image/jpeg"

        val testData = getTestDataFromFile()

        val testFile = MockMultipartFile(
            "file",
            resourceName,
            mimeTypeValue,
            testData
        )

        // POST resource to MediaAPI
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/media/v1/resources")
                .file(testFile)
                .param("resourceName", resourceName)
                .param("mimeType", mimeTypeValue)
                .param("tags", "foo, bar")
                .param("createdByUser", "tyler.durden@hotmail.com")
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        // Get resource DTO from MediaAPI
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/media/v1/resources")
                .param("resourceName", resourceName)
                .param("mimeType", mimeTypeValue)
                .accept("application/JSON")
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        // Get url for resource payload from response JSON
        val responseJson = mvcResult.response.contentAsString
        val responseMap = objectMapper.readValue(responseJson, Map::class.java)
        val responseMediaResources = responseMap["mediaResourceDTOList"] as List<Map<*, *>>

        assertTrue(responseMediaResources.isNotEmpty())
        val firstResource = responseMediaResources[0]
        assertTrue(firstResource.isNotEmpty())
        val responsePayloadURI = firstResource["payloadURI"] as String

        val expectedHost = "http://localhost:80"
        val expectedResourcePayloadPath = "/api/media/v1/resources/payload"
        val expectedFileName = "testimage1.jpg"
        val expectedMimeType = "image/jpeg"

        val expectedUrl =
            "$expectedHost$expectedResourcePayloadPath?resourceName=$expectedFileName&mimeType=$expectedMimeType"
        assertEquals(expectedUrl, responsePayloadURI)

        // Get the resource payload from MediaAPI
        val payloadResult = mockMvc.perform(
            MockMvcRequestBuilders.get(expectedResourcePayloadPath)
                .param("resourceName", resourceName)
                .param("mimeType", mimeTypeValue)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        assertEquals(mimeTypeValue, payloadResult.response.getHeader("Content-Type"))
        assertEquals("attachment; filename=\"testimage1.jpg\"", payloadResult.response.getHeader("Content-Disposition"))

        // Delete resource
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/media/v1/resources")
                .param("resourceName", resourceName)
                .param("mimeType", mimeTypeValue)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    private fun getTestDataFromFile(): ByteArray {
        val imageContentStream: InputStream? =
            this::class.java.classLoader.getResourceAsStream("testdata/image/testimage1.jpg")
        return imageContentStream?.readAllBytes() ?: throw IllegalArgumentException("test file resource not found")
    }

    @Test
    fun testCsrfBlocksDeleteWith403() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/media/v1/resources")
                .param("resourceName", "some_name")
                .param("mimeType", "image/jpeg")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun testCsrfBlocksPostWith403() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/media/v1/resources")
                .param("resourceName", "some_name")
                .param("mimeType", "image/jpeg")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    @WithMockUser
    fun testUploadTwiceReturns409() {

        // Prepare resource create, download and delete
        val resourceName = "testimage1.jpg"
        val mimeTypeValue = "image/jpeg"

        val testData = getTestDataFromFile()

        val testFile = MockMultipartFile(
            "file",
            resourceName,
            mimeTypeValue,
            testData
        )

        val mockMvcRequestBuilder = MockMvcRequestBuilders.multipart("/api/media/v1/resources")
            .file(testFile)
            .param("resourceName", resourceName)
            .param("mimeType", mimeTypeValue)
            .param("tags", "foo, bar")
            .param("createdByUser", "tyler.durden@hotmail.com")
            .with(csrf())

        // POST resource to MediaAPI
        mockMvc.perform(mockMvcRequestBuilder).andExpect(MockMvcResultMatchers.status().isCreated)

        // POST resource to MediaAPI again
        mockMvc.perform(mockMvcRequestBuilder).andExpect(MockMvcResultMatchers.status().isConflict)

        // Delete resource (test clean up)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/media/v1/resources")
                .param("resourceName", resourceName)
                .param("mimeType", mimeTypeValue)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}
