package de.dmalo.mediarepository.query

import de.dmalo.common.logging.InjectLogger
import de.dmalo.common.logging.LoggerDelegate
import de.dmalo.mediarepository.exception.SessionExpiredException
import org.springframework.stereotype.Component
import javax.jcr.Session
import javax.jcr.query.Query
import javax.jcr.query.QueryResult

@Component
internal class JcrQueryExecutor {

    @InjectLogger
    private val logger by LoggerDelegate()

    fun executeQuery(session: Session, queryString: String): QueryResult {
        if (!session.isLive) throw SessionExpiredException()
        logger.debug("Opening session.workspace.queryManager and executing '$queryString'")
        val queryManager = session.workspace.queryManager
        val query = queryManager.createQuery(queryString, Query.JCR_SQL2)
        val queryResult = query.execute()
        logger.debug("QueryResult has {} nodes and {} rows", queryResult.nodes, queryResult.rows)
        return queryResult
    }
}
