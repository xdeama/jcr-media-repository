package de.dmalo.mediarepository.configuration

import javax.jcr.Repository

interface JcrNamespaceInitializer {
    fun initialize(repository: Repository)
}
