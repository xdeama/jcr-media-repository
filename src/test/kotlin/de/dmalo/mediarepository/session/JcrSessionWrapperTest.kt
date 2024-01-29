package de.dmalo.mediarepository.session

import de.dmalo.mediarepository.springconfig.FileStoreConfig
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import de.dmalo.mediarepository.springconfig.OakConfig
import org.apache.jackrabbit.api.JackrabbitRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import javax.jcr.Session
import javax.jcr.SimpleCredentials

@ExtendWith(MockitoExtension::class)
class JcrSessionWrapperTest {

    @Mock
    lateinit var repository: JackrabbitRepository

    @Mock
    lateinit var config: MediaRepositoryContextProperties

    @Mock
    lateinit var session: Session

    private lateinit var jcrSessionWrapper: JcrSessionWrapper

    @BeforeEach
    fun setUp() {
        val fileStoreConfig = FileStoreConfig(
            "/path/to/store",
            256,
            "username",
            "password"
        )
        val oakConfig = OakConfig(fileStoreConfig)
        `when`(config.oak).thenReturn(oakConfig)
        `when`(repository.login(any(SimpleCredentials::class.java))).thenReturn(session)
        jcrSessionWrapper = JcrSessionWrapper(repository, config)
    }

    @Test
    fun testOpenSession() {
        verify(repository).login(any(SimpleCredentials::class.java))
        verify(session, never()).save()
        verify(session, never()).logout()
    }

    @Test
    fun testgetSession() {
        val result = jcrSessionWrapper.getSession()
        assertEquals(result, session)
    }

    @Test
    fun testClose() {
        jcrSessionWrapper.close()

        verify(session).save()
        verify(session).logout()
    }
}
