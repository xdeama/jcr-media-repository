package de.dmalo.mediarepository.session

import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import java.io.Closeable
import javax.jcr.Repository
import javax.jcr.Session
import javax.jcr.SimpleCredentials

class JcrSessionWrapper(
    private val repository: Repository,
    private val config: MediaRepositoryContextProperties
) : Closeable {

    private val session: Session

    init {
        session = openSession()
    }

    private fun openSession(): Session {
        return repository.login(
            SimpleCredentials(
                config.oak.fileStore.username,
                config.oak.fileStore.password.toCharArray()
            )
        )
    }

    fun getSession(): Session = session

    override fun close() {
        saveAndClose()
    }

    private fun saveAndClose() {
        session.save()
        session.logout()
    }
}
