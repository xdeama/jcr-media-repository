package de.dmalo.mediarepository.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import javax.jcr.Node
import javax.jcr.NodeIterator
import javax.jcr.Value
import javax.jcr.query.QueryResult
import javax.jcr.query.Row
import javax.jcr.query.RowIterator

class JcrQueryResultInterpreterTest {

    private val queryResult: QueryResult = mock()

    private val jcrQueryResultInterpreter = JcrQueryResultInterpreter()

    @Test
    fun hasResult() {
        val rows: RowIterator = mock()

        `when`(queryResult.rows).thenReturn(rows)
        `when`(rows.size).thenReturn(1L)

        val result = jcrQueryResultInterpreter.hasResult(queryResult)
        assertTrue(result)
    }

    @Test
    fun extractFilePaths() {
        val rows: RowIterator = mock()
        val row: Row = mock()
        val value: Value = mock()

        `when`(queryResult.rows).thenReturn(rows)
        `when`(rows.hasNext())
            .thenReturn(true)
            .thenReturn(false)
        `when`(rows.nextRow()).thenReturn(row)
        `when`(row.getValue(eq("filePath"))).thenReturn(value)
        `when`(value.string).thenReturn("filePath1")
        val result = jcrQueryResultInterpreter.extractFilePaths(queryResult)
        assertEquals("filePath1", result[0])

        verify(rows, times(2)).hasNext()
        verify(rows).nextRow()
        verify(row).getValue(eq("filePath"))
    }

    @Test
    fun getNodesFromResult() {
        val node: Node = mock()
        val nodeIterator: NodeIterator = mock()
        `when`(queryResult.nodes).thenReturn(nodeIterator)
        `when`(nodeIterator.hasNext()).thenReturn(true, false)
        `when`(nodeIterator.next()).thenReturn(node)

        val result: List<Node> = jcrQueryResultInterpreter.getNodesFromResult(queryResult)

        verify(nodeIterator).next()

        assertEquals(node, result[0])
    }
}
