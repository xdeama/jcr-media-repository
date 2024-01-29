package de.dmalo.mediarepository.exception

import javax.jcr.RepositoryException

class ResourceAlreadyExistsException(message: String) : RepositoryException(message)
