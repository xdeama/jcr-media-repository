package de.dmalo.mediarepository.configuration

import javax.jcr.Repository

interface JcrRepositoryInitializer {
    fun initializeRepository(repository: Repository): Repository
}
