package de.dmalo.mediarepository.configuration

import javax.jcr.Repository

interface JcrNodeStructureInitializer {
    fun initialize(repository: Repository)
}
