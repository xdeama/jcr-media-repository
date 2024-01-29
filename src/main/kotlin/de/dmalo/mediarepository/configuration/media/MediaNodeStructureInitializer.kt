package de.dmalo.mediarepository.configuration.media

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.configuration.JcrNodeStructureInitializer
import de.dmalo.mediarepository.model.types.JcrNodeType
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.model.types.SectionType
import de.dmalo.mediarepository.node.JcrMediaNodePathAssembler
import de.dmalo.mediarepository.session.JcrSessionWrapper
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.apache.jackrabbit.commons.JcrUtils
import org.springframework.stereotype.Component
import javax.jcr.Repository

@Component
internal class MediaNodeStructureInitializer(
    private val config: MediaRepositoryContextProperties
) : JcrNodeStructureInitializer {

    @InjectLogger
    private val logger by LoggerDelegate()

    override fun initialize(repository: Repository) {
        val allCategories = MimeType.allCategoryNodeNames()
        val allMimeTypes = MimeType.entries.toTypedArray()

        try {
            JcrSessionWrapper(repository, config).use { sessionWrapper ->
                val rootNode = sessionWrapper.getSession().rootNode
                val mediaCategoryNode = rootNode.addNode(SectionType.MEDIA.nodeName, JcrNodeType.MEDIA_SECTION.key)

                allCategories.map {
                    mediaCategoryNode.addNode(it, JcrNodeType.MEDIA_CATEGORY.key)
                }

                allMimeTypes.map {
                    val path = JcrMediaNodePathAssembler.getTypePath(it.category.nodeName, it.fileExtension)
                    JcrUtils.getOrCreateByPath(
                        path, JcrNodeType.MEDIA_MIMETYPE.key, sessionWrapper.getSession()
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error("Error initializing Apache Oak Jackrabbit repository. Repository is malformed and unusable!")
            throw ex
        }
    }
}
