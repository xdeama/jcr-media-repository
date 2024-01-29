package de.dmalo.mediarepository.exception

import javax.jcr.RepositoryException

class SessionExpiredException(
    override val message: String = "JCR session has been closed. Operation not possible."
) : RepositoryException(message)
