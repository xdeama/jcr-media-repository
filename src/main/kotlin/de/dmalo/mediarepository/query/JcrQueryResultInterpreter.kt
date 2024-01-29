package de.dmalo.mediarepository.query

import org.springframework.stereotype.Component
import javax.jcr.Node
import javax.jcr.query.QueryResult

@Component
internal class JcrQueryResultInterpreter {

    fun hasResult(queryResult: QueryResult): Boolean {
        val rows = queryResult.rows
        return rows.size > 0L
    }

    fun extractFilePaths(queryResult: QueryResult): List<String> {
        val resultList = mutableListOf<String>()
        val rows = queryResult.rows
        while (rows.hasNext()) {
            val row = rows.nextRow()
            val filePath = row.getValue("filePath")?.string ?: "No Path"
            resultList.add(filePath)
        }
        return resultList
    }

    fun getNodesFromResult(queryResult: QueryResult): List<Node> {
        val nodeList = mutableListOf<Node>()
        queryResult.nodes.forEach { node ->
            if (node is Node) {
                nodeList.add(node)
            }
        }
        return nodeList
    }
}
