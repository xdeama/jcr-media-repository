package de.dmalo.mediarepository.configuration

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.apache.jackrabbit.api.JackrabbitSession
import org.apache.jackrabbit.api.security.user.User
import org.springframework.stereotype.Component
import javax.jcr.LoginException
import javax.jcr.Repository
import javax.jcr.SimpleCredentials

@Component
internal class JcrRepositoryInitializerImpl(
    private val config: MediaRepositoryContextProperties,
    private val jcrNamespaceInitializerList: List<JcrNamespaceInitializer>,
    private val customNodeTypeInitializerList: List<JcrCustomTypeConfiguration>,
    private val jcrNodeStructureInitializerList: List<JcrNodeStructureInitializer>,
) : JcrRepositoryInitializer {

    @InjectLogger
    private val logger by LoggerDelegate()

    companion object {
        private const val DEFAULT_ADMIN_PASSWORD = "admin"
    }

    override fun initializeRepository(repository: Repository): Repository {
        if (!repositoryRequiresSetup(repository)) {
            logger.info("Mounting existing JCR repository at '${config.oak.fileStore.path}'. Skipping initialization.")
            return repository
        }

        logger.info("Initializing new JCR Oak repository at '${config.oak.fileStore.path}'")
        try {
            setDefaultUserCredentials(repository)

            jcrNamespaceInitializerList.forEach { initializer -> initializer.initialize(repository) }
            customNodeTypeInitializerList.forEach { initializer -> initializer.initialize(repository) }
            jcrNodeStructureInitializerList.forEach { initializer -> initializer.initialize(repository) }
        } catch (ex: Exception) {
            logger.error(
                "Error occurred during repository initialization. If the stacktrace contains a" +
                        "NIO exception caused by concurrent file access, it is likely a node type, property or namespace " +
                        "configuration error in disguise. You may prefer checking namespaces, node names and property " +
                        "names first. Original Exception message: ${ex.message}"
            )
            throw ex
        }

        logger.debug("Finished initializing new JCR Oak repository")
        return repository
    }

    private fun repositoryRequiresSetup(repository: Repository): Boolean {
        try {
            val session = repository.login(
                SimpleCredentials(config.oak.fileStore.username, DEFAULT_ADMIN_PASSWORD.toCharArray())
            )
            session.logout()
            return true
        } catch (ex: LoginException) {
            return false
        }
    }

    private fun setDefaultUserCredentials(repository: Repository) {
        try {
            val session = repository.login(
                SimpleCredentials(config.oak.fileStore.username, DEFAULT_ADMIN_PASSWORD.toCharArray())
            )

            try {
                val userManager = (session as JackrabbitSession).userManager
                val user = userManager.getAuthorizable(config.oak.fileStore.username) as User?
                if (user != null) {
                    user.changePassword(config.oak.fileStore.password)
                    logger.warn("New Oak Jackrabbit Tar Repository created, setting admin credentials")
                    session.save()
                }
            } catch (ex: LoginException) {
                logger.error("Initializing admin credentials failed. Default credentials login error: ${ex.message}")

            } finally {
                session.logout()
            }

        } catch (loginException: LoginException) {
            logger.info("Initializing admin credentials was skipped, repository has custom password")
        }
    }
}
