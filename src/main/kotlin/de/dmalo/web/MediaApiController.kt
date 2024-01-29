package de.dmalo.web

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.exception.*
import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.CategoryType
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.repository.JcrMediaRepository
import de.dmalo.web.model.MediaResourceDTO
import de.dmalo.web.model.MediaResourceDTOWrapperList
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream

@RestController
@RequestMapping("/api/media/v1")
class MediaApiController(
    @Autowired private val jcrMediaRepository: JcrMediaRepository,
) {

    @InjectLogger
    private val logger by LoggerDelegate()

    @GetMapping("/resources", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getResources(
        @RequestParam(required = false) mimeType: String?,
        @RequestParam(required = false) resourceName: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) category: String?,
        request: HttpServletRequest
    ): ResponseEntity<MediaResourceDTOWrapperList> {
        logger.debug("GET request received: ${request.requestURI}")

        val fileResources: List<BinaryFileResource> = when {
            mimeType != null && resourceName != null -> {
                listOf(jcrMediaRepository.get(MimeType.fromString(mimeType), resourceName))
            }

            mimeType != null -> {
                jcrMediaRepository.getByMimeType(MimeType.fromString(mimeType))
            }

            tag != null -> {
                jcrMediaRepository.getByTag(tag)
            }

            category != null -> {
                jcrMediaRepository.getByCategory(CategoryType.fromString(category))
            }

            else -> {
                jcrMediaRepository.getAll()
            }
        }

        val responseObject = map(fileResources, request)
        logger.debug("GET request: ${request.requestURI} returned HTTP 200")
        return ResponseEntity.status(HttpStatus.OK).body(responseObject)
    }

    private fun map(fileResources: List<BinaryFileResource>, request: HttpServletRequest): MediaResourceDTOWrapperList {

        val mediaResourceDTOs = fileResources.map { binaryFileResource ->
            val resourceName = binaryFileResource.fileName
            val mimeType = binaryFileResource.mimeType
            val payloadURI = assembleResourcePayloadUrl(request, resourceName, mimeType)
            MediaResourceDTO(binaryFileResource, payloadURI)
        }
        return MediaResourceDTOWrapperList(mediaResourceDTOs)
    }

    private fun assembleResourcePayloadUrl(
        request: HttpServletRequest,
        resourceName: String,
        mimeType: MimeType
    ): String {
        val originalRequestServerUrl = getOriginalRequestServerUrl(request)
        val restEndpointPath = "/api/media/v1/resources/payload"
        val urlParameter = "?resourceName=$resourceName&mimeType=${mimeType.typeString}"
        return originalRequestServerUrl + restEndpointPath + urlParameter
    }

    private fun getOriginalRequestServerUrl(request: HttpServletRequest): String {
        val protocol = if (request.isSecure) "https" else "http"
        return "$protocol://${request.serverName}:${request.serverPort}"
    }

    @PostMapping("/resources")
    fun postResource(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("resourceName") resourceName: String,
        @RequestParam("mimeType") mimeType: String,
        @RequestParam("encoding") encoding: String?,
        @RequestParam("tags") tags: List<String>,
        @RequestParam("createdByUser") createdByUser: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        logger.debug("POST request received: ${request.requestURI}")

        try {
            val binaryFileResource = BinaryFileResourceImpl(
                data = file.bytes.inputStream(),
                fileName = resourceName,
                mimeType = mimeType,
                binaryEncoding = encoding,
                fileSizeInBytes = file.size.toInt(),
                creatorUserName = createdByUser,
                tags = tags
            )
            jcrMediaRepository.create(binaryFileResource)
            logger.debug("POST request: ${request.requestURI} returned HTTP 201")
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully")
        } catch (ex: Exception) {
            logger.error("Error uploading file for ${request.requestURI} Exception: ${ex.message}")
            throw ex
        }
    }

    @PutMapping("/resources")
    fun putResource(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("resourceName") resourceName: String,
        @RequestParam("mimeType") mimeType: String,
        @RequestParam("encoding") encoding: String?,
        @RequestParam("tags") tags: List<String>,
        @RequestParam("createdByUser") createdByUser: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        logger.debug("PUT request received: ${request.requestURI}")

        try {
            val binaryFileResource = BinaryFileResourceImpl(
                data = file.bytes.inputStream(),
                fileName = resourceName,
                mimeType = mimeType,
                binaryEncoding = encoding,
                fileSizeInBytes = file.size.toInt(),
                creatorUserName = createdByUser,
                tags = tags
            )
            jcrMediaRepository.createOrReplace(binaryFileResource)
            logger.debug("PUT request: ${request.requestURI} returned HTTP 201")
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully")
        } catch (ex: Exception) {
            logger.error("Error uploading file for ${request.requestURI} Exception: ${ex.message}")
            throw ex
        }
    }

    @DeleteMapping("/resources")
    fun deleteResource(
        @RequestParam mimeType: String,
        @RequestParam resourceName: String
    ): ResponseEntity<String> {
        jcrMediaRepository.delete(MimeType.fromString(mimeType), resourceName)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @GetMapping("/resources/payload")
    fun getResourceContent(
        @RequestParam mimeType: String,
        @RequestParam resourceName: String,
        request: HttpServletRequest
    ): ResponseEntity<StreamingResponseBody> {
        logger.debug("GET request received: ${request.requestURI}")
        val fileResource = jcrMediaRepository.get(MimeType.fromString(mimeType), resourceName)

        val headers = HttpHeaders()
        headers.set("Content-Type", fileResource.mimeType.typeString)
        headers.set("Content-Disposition", "attachment; filename=\"$resourceName\"")
        headers.set("Content-Length", fileResource.fileSizeInBytes.toString())
        headers.set("Cache-Control", "no-cache")

        logger.debug("GET request: ${request.requestURI} HTTP 200")
        return ResponseEntity(
            StreamingResponseBody { outputStream: OutputStream ->
                val dataStream = fileResource.data
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (dataStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                dataStream.close()
            },
            headers,
            HttpStatus.OK
        )
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(ex: ResourceAlreadyExistsException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)
    }


    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }

    @ExceptionHandler(
        EncodingTypeNotSupportedException::class,
        MimeTypeNotSupportedException::class,
        CategoryTypeNotSupportedException::class
    )
    fun handleBadRequestException(ex: RuntimeException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
    }

}
