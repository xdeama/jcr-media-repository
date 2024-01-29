package de.dmalo.mediarepository.exception

import javax.jcr.RepositoryException

class FileSizeLimitExceededException(message: String) : RepositoryException(message)
