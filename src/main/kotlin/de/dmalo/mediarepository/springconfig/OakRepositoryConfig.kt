package de.dmalo.mediarepository.springconfig

import de.dmalo.mediarepository.configuration.JcrRepositoryInitializer
import org.apache.jackrabbit.oak.Oak
import org.apache.jackrabbit.oak.jcr.Jcr
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import javax.jcr.Repository

@Configuration
open class OakRepositoryConfig(
    private val config: MediaRepositoryContextProperties,
    private val jcrRepositoryInitializer: JcrRepositoryInitializer
) {
    @Bean
    open fun jackrabbitOakRepository(): Repository {
        val fileStore = FileStoreBuilder
            .fileStoreBuilder(File(config.oak.fileStore.path))
            .withMaxFileSize(config.oak.fileStore.maxFileSize)
            .build()
        val segmentNodeStore = SegmentNodeStoreBuilders.builder(fileStore).build()
        val repository = Jcr(Oak(segmentNodeStore)).createRepository()
        return jcrRepositoryInitializer.initializeRepository(repository)
    }
}
