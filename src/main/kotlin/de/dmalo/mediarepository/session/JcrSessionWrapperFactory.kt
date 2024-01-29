package de.dmalo.mediarepository.session

import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.stereotype.Component
import javax.jcr.Repository

@Component
class JcrSessionWrapperFactory(
    private val repository: Repository,
    private val config: MediaRepositoryContextProperties
) {

    fun createOakSessionWrapper(): JcrSessionWrapper = JcrSessionWrapper(repository, config)

}
