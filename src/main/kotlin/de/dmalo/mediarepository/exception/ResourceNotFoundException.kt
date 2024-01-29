package de.dmalo.mediarepository.exception

import javax.jcr.RepositoryException

class ResourceNotFoundException(message: String) : RepositoryException(message)
