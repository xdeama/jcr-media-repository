package de.dmalo.mediarepository.configuration

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.session.JcrSessionWrapper
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.stereotype.Component
import javax.jcr.NamespaceException
import javax.jcr.Repository

@Component
abstract class AbstractNamespaceInitializer(
    private val config: MediaRepositoryContextProperties
) : JcrNamespaceInitializer {

    @InjectLogger
    private val logger by LoggerDelegate()

    abstract val baseUri: String
    abstract val namespace: String

    override fun initialize(repository: Repository) {
        JcrSessionWrapper(repository, config).use { sessionWrapper ->
            val namespaceRegistry = sessionWrapper.getSession().workspace.namespaceRegistry
            val uri = "$baseUri/$namespace/1.0"
            try {
                namespaceRegistry.registerNamespace(namespace, uri)
            } catch (e: NamespaceException) {
                logger.info("Adding JCR namespace '$namespace' failed, namespace already exists")
            }
        }
    }
}
