package de.dmalo.mediarepository.configuration.media

import de.dmalo.mediarepository.configuration.AbstractNamespaceInitializer
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.stereotype.Component

@Component
internal class MediaNamespaceInitializer(
    config: MediaRepositoryContextProperties
) : AbstractNamespaceInitializer(config) {
    override val namespace = "media"
    override val baseUri = "https://dmalo.de/jcr/oak"
}
