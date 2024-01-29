package de.dmalo.mediarepository.springconfig

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "media-repository")
class MediaRepositoryContextProperties(
    val oak: OakConfig
)

class OakConfig(
    val fileStore: FileStoreConfig,
)

class FileStoreConfig(
    val path: String,
    val maxFileSize: Int,
    val username: String,
    val password: String
)
