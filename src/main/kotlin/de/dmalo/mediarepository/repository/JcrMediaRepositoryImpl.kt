package de.dmalo.mediarepository.repository

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.exception.ResourceAlreadyExistsException
import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.mapper.FileResourceMapper
import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.types.CategoryType
import de.dmalo.mediarepository.model.types.JcrNodeProperty
import de.dmalo.mediarepository.model.types.JcrNodeType
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.node.JcrMediaNodePathAssembler
import de.dmalo.mediarepository.query.JcrQueryExecutor
import de.dmalo.mediarepository.query.JcrQueryResultInterpreter
import de.dmalo.mediarepository.session.JcrSessionWrapperFactory
import org.springframework.stereotype.Repository
import javax.jcr.PathNotFoundException
import javax.jcr.RepositoryException

@Repository
internal class JcrMediaRepositoryImpl(
    private val jcrSessionWrapperFactory: JcrSessionWrapperFactory,
    private val jcrQueryExecutor: JcrQueryExecutor,
    private val jcrQueryResultInterpreter: JcrQueryResultInterpreter,
    private val fileResourceMapper: FileResourceMapper,
) : JcrMediaRepository {

    @InjectLogger
    private val logger by LoggerDelegate()

    override fun has(mimeType: MimeType, resourceName: String): Boolean {
        val fileNodePath = JcrMediaNodePathAssembler.getFileNodePath(mimeType, resourceName)

        try {
            jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
                sessionWrapper.getSession().getNode(fileNodePath)
                return true
            }
        } catch (ex: PathNotFoundException) {
            logger.debug("exists check for JCR resource $fileNodePath is false.")
            return false
        } catch (ex: RepositoryException) {
            logger.error("Exception occurred retrieving resource $resourceName from Oak Repository: ${ex.message}")
            throw ex
        }
    }

    @Throws(ResourceNotFoundException::class)
    override fun get(mimeType: MimeType, resourceName: String): BinaryFileResource {
        val fileNodePath = JcrMediaNodePathAssembler.getFileNodePath(mimeType, resourceName)

        try {
            jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
                val fileNode = sessionWrapper.getSession().getNode(fileNodePath)
                return fileResourceMapper.copyJcrNodesToFileResource(fileNode)
            }
        } catch (ex: PathNotFoundException) {
            logger.info("JCR resource $fileNodePath not found.")
            throw ResourceNotFoundException("Resource $resourceName of type ${mimeType.typeString} not found.")
        } catch (ex: RepositoryException) {
            logger.error("Exception occurred retrieving resource $resourceName from Oak Repository: ${ex.message}")
            throw ex
        }
    }

    override fun listAllFilePaths(): List<String> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.[${JcrNodeProperty.JCR_PATH.key}] AS filePath
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            return jcrQueryResultInterpreter.extractFilePaths(queryResult)
        }
    }

    override fun getAll(): List<BinaryFileResource> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.*
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            val fileNodes = jcrQueryResultInterpreter.getNodesFromResult(queryResult)
            return fileNodes.map { node -> fileResourceMapper.copyJcrNodesToFileResource(node) }
        }
    }

    override fun getByMimeType(mimeType: MimeType): List<BinaryFileResource> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.*
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
                INNER JOIN [${JcrNodeType.MEDIA_RESOURCE.key}] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[${JcrNodeProperty.JCR_MIMETYPE.key}] = ${mimeType.typeString}
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            val fileNodes = jcrQueryResultInterpreter.getNodesFromResult(queryResult)
            return fileNodes.map { node -> fileResourceMapper.copyJcrNodesToFileResource(node) }
        }
    }

    override fun listFilePathsByMimeType(mimeType: MimeType): List<String> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.[${JcrNodeProperty.JCR_PATH.key}] AS filePath
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
                INNER JOIN [${JcrNodeType.MEDIA_RESOURCE.key}] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[${JcrNodeProperty.JCR_MIMETYPE.key}] = ${mimeType.typeString}
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            return jcrQueryResultInterpreter.extractFilePaths(queryResult)
        }
    }

    override fun getByCategory(category: CategoryType): List<BinaryFileResource> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.*
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
                INNER JOIN [${JcrNodeType.MEDIA_CATEGORY.key}] AS category ON ISCHILDNODE(file, category)
                WHERE category.[${JcrNodeProperty.JCR_NODENAME.key}] = ${category.nodeName}
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            val fileNodes = jcrQueryResultInterpreter.getNodesFromResult(queryResult)
            return fileNodes.map { node -> fileResourceMapper.copyJcrNodesToFileResource(node) }
        }
    }

    override fun getByTag(tag: String): List<BinaryFileResource> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.*
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
                INNER JOIN [${JcrNodeType.MEDIA_RESOURCE.key}] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[${JcrNodeProperty.TAGS.key}] = '$tag'
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            val fileNodes = jcrQueryResultInterpreter.getNodesFromResult(queryResult)
            return fileNodes.map { node -> fileResourceMapper.copyJcrNodesToFileResource(node) }
        }
    }

    override fun listFilePathsByCategory(category: CategoryType): List<String> {
        jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
            val queryString = """
                SELECT file.[${JcrNodeProperty.JCR_PATH.key}] AS filePath
                FROM [${JcrNodeType.MEDIA_FILE.key}] AS file
                INNER JOIN [${JcrNodeType.MEDIA_CATEGORY.key}] AS category ON ISCHILDNODE(file, category)
                WHERE category.[${JcrNodeProperty.JCR_NODENAME.key}] = ${category.nodeName}
            """.trimIndent()
            val queryResult = jcrQueryExecutor.executeQuery(sessionWrapper.getSession(), queryString)
            return jcrQueryResultInterpreter.extractFilePaths(queryResult)
        }
    }

    override fun create(binaryFileResource: BinaryFileResource) {
        logger.debug("Saving {} to media repository", binaryFileResource)

        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        try {
            jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->

                val typeNode = sessionWrapper.getSession().getNode(typeNodePath)
                if (typeNode == null) {
                    val message = "Saving ${binaryFileResource.fileName} to media repository failed. " +
                            "No type node found at $typeNodePath"
                    logger.error(message)
                    throw ResourceNotFoundException(message)
                }

                if (typeNode.hasNode(binaryFileResource.fileName)) {
                    val message =
                        "Saving new resource failed, resource at '${typeNode.path}/${binaryFileResource.fileName}' " +
                                "already exists"
                    logger.error(message)
                    throw ResourceAlreadyExistsException(message)
                }

                fileResourceMapper.copyFileResourceToJcrNodes(binaryFileResource, typeNode)
            }
        } catch (ex: RepositoryException) {
            logger.error(
                "Exception occurred creating resource ${binaryFileResource.fileName} " +
                        "to Oak repository: ${ex.message}"
            )
            throw ex
        }
    }

    override fun createOrReplace(binaryFileResource: BinaryFileResource) {
        val filePath = JcrMediaNodePathAssembler.getFileNodePath(binaryFileResource)
        try {
            jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
                val session = sessionWrapper.getSession()
                try {
                    val node = session.getNode(filePath)
                    node.remove()
                } catch (_: ResourceNotFoundException) {
                } catch (_: PathNotFoundException) {
                } finally {
                    create(binaryFileResource)
                }
            }
        } catch (ex: RepositoryException) {
            logger.error(
                "Exception occurred createOrReplace resource ${binaryFileResource.fileName} " +
                        "to Oak repository: ${ex.message}"
            )
            throw ex
        }
    }

    @Throws(ResourceNotFoundException::class)
    override fun delete(mimeType: MimeType, resourceName: String) {
        logger.debug("Deleting $resourceName (${mimeType.typeString}) from api repository")
        val filePath = JcrMediaNodePathAssembler.getFileNodePath(mimeType, resourceName)

        try {
            jcrSessionWrapperFactory.createOakSessionWrapper().use { sessionWrapper ->
                val session = sessionWrapper.getSession()

                if (!session.nodeExists(filePath)) {
                    logger.error("Deleting resource failed, resource at '$filePath' does not exist")
                    throw ResourceNotFoundException("Deleting resource failed, resource at '$filePath' does not exist")
                }

                val node = session.getNode(filePath)
                node.remove()
            }
        } catch (ex: RepositoryException) {
            logger.error("Exception occurred deleting resource $resourceName to Oak repository: ${ex.message}")
            throw ex
        }
    }
}
