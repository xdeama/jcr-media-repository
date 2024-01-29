package de.dmalo.integration.testcontext

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class MediaLibraryTestContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private const val DEFAULT_FILESTOREPATH = "target/tmp-storage/media-repository/"
        private const val DEFAULT_MAXFILESIZE = "10000000"
        private const val DEFAULT_OAK_USERNAME = "admin"
        private const val DEFAULT_OAK_PASSWORD = "itpassword"
    }

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        val fileStorePath = System.getenv("OAK_FILESTOREPATH") ?: DEFAULT_FILESTOREPATH
        val maxFileSize = System.getenv("OAK_MAXFILESIZE") ?: DEFAULT_MAXFILESIZE
        val username = System.getenv("OAK_USERNAME") ?: DEFAULT_OAK_USERNAME
        val password = System.getenv("OAK_PASSWORD") ?: DEFAULT_OAK_PASSWORD
        TestPropertyValues.of(
            "media-repository.oak.file-store.path=$fileStorePath",
            "media-repository.oak.file-store.max-file-size=$maxFileSize",
            "media-repository.oak.file-store.username=$username",
            "media-repository.oak.file-store.password=$password"
        ).applyTo(configurableApplicationContext.environment)
    }
}
