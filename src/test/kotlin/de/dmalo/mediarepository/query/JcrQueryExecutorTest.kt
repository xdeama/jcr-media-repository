package de.dmalo.mediarepository.query

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import javax.jcr.Session
import javax.jcr.Workspace
import javax.jcr.query.Query
import javax.jcr.query.QueryManager
import javax.jcr.query.QueryResult

class JcrQueryExecutorTest {

    private val session = Mockito.mock<Session>()
    private val workspace = Mockito.mock<Workspace>()
    private val queryManager = Mockito.mock<QueryManager>()
    private val query = Mockito.mock<Query>()
    private val queryResult = Mockito.mock<QueryResult>()

    private val jcrQueryExecutor = JcrQueryExecutor()

    @Test
    fun executeQuery() {
        Mockito.`when`(session.isLive).thenReturn(true)
        Mockito.`when`(session.workspace).thenReturn(workspace)
        Mockito.`when`(workspace.queryManager).thenReturn(queryManager)
        Mockito.`when`(queryManager.createQuery(ArgumentMatchers.anyString(), ArgumentMatchers.eq(Query.JCR_SQL2)))
            .thenReturn(query)
        Mockito.`when`(query.execute()).thenReturn(queryResult)

        val result = jcrQueryExecutor.executeQuery(session, "dummy query string")
        Assertions.assertEquals(queryResult, result)
    }
}
