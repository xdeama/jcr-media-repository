package de.dmalo.mediarepository.configuration

import javax.jcr.Repository

interface JcrCustomTypeConfiguration {
    fun initialize(repository: Repository)
}
