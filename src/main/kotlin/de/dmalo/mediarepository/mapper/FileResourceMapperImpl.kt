package de.dmalo.mediarepository.mapper

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.exception.FileSizeLimitExceededException
import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.exception.SessionExpiredException
import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.*
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.stereotype.Component
import javax.jcr.Binary
import javax.jcr.Node

@Component
internal class FileResourceMapperImpl(private val config: MediaRepositoryContextProperties) : FileResourceMapper {

    @InjectLogger
    private val logger by LoggerDelegate()

    override fun copyJcrNodesToFileResource(fileNode: Node): BinaryFileResource {
        if (!fileNode.session.isLive) throw SessionExpiredException()

        val fileName = fileNode.name

        val resourceNode = fileNode.getNode(JcrNodeNames.JCR_CONTENT.key)
        if (!fileNode.isNodeType(JcrNodeType.MEDIA_FILE.key) ||
            !resourceNode.isNodeType(JcrNodeType.MEDIA_RESOURCE.key)
        ) {
            val message =
                "Creating BinaryFileResource from Jcr nodes has failed: fileNode ${fileNode.path} is malformed. " +
                        "Resource node not found at path: ${fileNode.path}/${JcrNodeNames.JCR_CONTENT.key}."
            logger.error(message)
            throw ResourceNotFoundException(message)
        }

        val binaryData = resourceNode.getProperty(JcrNodeProperty.JCR_DATA.key).binary.stream.readAllBytes()
        val binaryDataStream = binaryData.inputStream()
        val encodingType = EncodingType.fromString(resourceNode.getProperty(JcrNodeProperty.JCR_ENCODING.key).string)
        val size = resourceNode.getProperty(JcrNodeProperty.FILE_SIZE.key).long.toInt()
        val mimeType = MimeType.fromString(resourceNode.getProperty(JcrNodeProperty.JCR_MIMETYPE.key).string)
        val lastModifiedByUser = resourceNode.getProperty(JcrNodeProperty.JCR_LAST_MODIFIED_BY.key).string
        val lastModifiedDate = resourceNode.getProperty(JcrNodeProperty.JCR_LAST_MODIFIED.key).date
        val createdByUser = resourceNode.getProperty(JcrNodeProperty.CREATED_BY.key).string
        val createdDate = resourceNode.getProperty(JcrNodeProperty.JCR_CREATED.key).date
        val tags = resourceNode.getProperty(JcrNodeProperty.TAGS.key).values.map { it.string }

        return BinaryFileResourceImpl(
            binaryDataStream,
            fileName,
            mimeType,
            encodingType,
            size,
            tags,
            createdByUser,
            createdDate,
            lastModifiedByUser,
            lastModifiedDate
        )
    }

    override fun copyFileResourceToJcrNodes(binaryFileResource: BinaryFileResource, parentNode: Node): Node {
        try {
            val fileNode = parentNode.addNode(binaryFileResource.fileName, JcrNodeType.MEDIA_FILE.key)
            val resourceNode = fileNode.addNode(JcrNodeNames.JCR_CONTENT.key, JcrNodeType.MEDIA_RESOURCE.key)
            val binaryData: Binary = resourceNode.session.valueFactory.createBinary(binaryFileResource.data)

            if (binaryData.size > (config.oak.fileStore.maxFileSize)) {
                val message = "Saving ${binaryFileResource.fileName} to JCR" +
                        " failed. File size limit of ${config.oak.fileStore.maxFileSize} exceeded."
                logger.error(message)
                throw FileSizeLimitExceededException(message)
            }

            resourceNode.setProperty(JcrNodeProperty.JCR_DATA.key, binaryData)
            resourceNode.setProperty(
                JcrNodeProperty.JCR_ENCODING.key,
                binaryFileResource.binaryEncoding?.typeString ?: "NONE"
            )
            resourceNode.setProperty(JcrNodeProperty.JCR_MIMETYPE.key, binaryFileResource.getMimeTypeTypeString())
            resourceNode.setProperty(JcrNodeProperty.FILE_SIZE.key, binaryFileResource.fileSizeInBytes.toLong())
            resourceNode.setProperty(JcrNodeProperty.JCR_LAST_MODIFIED.key, binaryFileResource.lastModifiedDate)
            resourceNode.setProperty(JcrNodeProperty.JCR_LAST_MODIFIED_BY.key, binaryFileResource.lastModifiedByUser)
            resourceNode.setProperty(JcrNodeProperty.JCR_CREATED.key, binaryFileResource.lastModifiedDate)
            resourceNode.setProperty(JcrNodeProperty.CREATED_BY.key, binaryFileResource.lastModifiedByUser)
            resourceNode.setProperty(JcrNodeProperty.TAGS.key, binaryFileResource.tags.toTypedArray())

            return fileNode
        } catch (ex: Exception) {
            logger.error("Creating JCR nodes for BinaryFileResource ${binaryFileResource.fileName} has failed: ${ex.message}")
            throw ex
        }
    }
}
