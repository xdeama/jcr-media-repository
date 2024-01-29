package de.dmalo.mediarepository.repository

import de.dmalo.assertions.softAssert
import de.dmalo.mediarepository.exception.ResourceAlreadyExistsException
import de.dmalo.mediarepository.exception.ResourceNotFoundException
import de.dmalo.mediarepository.mapper.FileResourceMapper
import de.dmalo.mediarepository.model.BinaryFileResourceImpl
import de.dmalo.mediarepository.model.types.CategoryType
import de.dmalo.mediarepository.model.types.EncodingType
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.node.JcrMediaNodePathAssembler
import de.dmalo.mediarepository.query.JcrQueryExecutor
import de.dmalo.mediarepository.query.JcrQueryResultInterpreter
import de.dmalo.mediarepository.session.JcrSessionWrapper
import de.dmalo.mediarepository.session.JcrSessionWrapperFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import javax.jcr.Node
import javax.jcr.Session
import javax.jcr.query.QueryResult

class JcrMediaRepositoryImplTest {

    private val jcrSessionWrapperFactory: JcrSessionWrapperFactory = mock()
    private val jcrQueryExecutor: JcrQueryExecutor = mock()
    private val jcrQueryResultInterpreter: JcrQueryResultInterpreter = mock()
    private val fileResourceMapper: FileResourceMapper = mock()
    private val sessionWrapper: JcrSessionWrapper = mock()
    private val session: Session = mock()
    private val queryResult: QueryResult = mock()
    private val node: Node = mock()

    private val binaryFileResource = BinaryFileResourceImpl(
        data = ByteArrayInputStream("test data".toByteArray()),
        fileName = "test",
        mimeType = MimeType.JPEG,
        binaryEncoding = EncodingType.UTF_8,
        fileSizeInBytes = 9,
        creatorUserName = "jUNIT",
        tags = listOf("unit", "testing")
    )

    private val jcrMediaRepositoryImpl = JcrMediaRepositoryImpl(
        jcrSessionWrapperFactory,
        jcrQueryExecutor,
        jcrQueryResultInterpreter,
        fileResourceMapper
    )

    @Test
    fun hasShouldReturnInstructorReturnedValue() {
        val fileNodePath = JcrMediaNodePathAssembler.getFileNodePath(MimeType.JPEG, "test")
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(fileNodePath))).thenReturn(fileNode)
        `when`(fileResourceMapper.copyJcrNodesToFileResource(fileNode)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.has(MimeType.JPEG, "test")

        assertTrue(result)
    }

    @Test
    fun getShouldReturnABinaryFileResourceWhenResourceExists() {
        val fileNodePath = JcrMediaNodePathAssembler.getFileNodePath(MimeType.JPEG, "test")
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(fileNodePath))).thenReturn(fileNode)
        `when`(fileResourceMapper.copyJcrNodesToFileResource(fileNode)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.get(MimeType.JPEG, "test")

        assertEquals(binaryFileResource, result)
    }

    @Test
    fun createShouldNotThrowWhenResourceDoesNotExist() {
        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)
        val typeNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(typeNode)

        assertDoesNotThrow {
            jcrMediaRepositoryImpl.create(binaryFileResource)
        }
    }

    @Test
    fun listAllFilePaths() {
        val expectedQuery = """
                SELECT file.[jcr:path] AS filePath
                FROM [media:file] AS file
            """.trimIndent()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.extractFilePaths(queryResult)).thenReturn(listOf("path1", "path2"))

        val expectedFilePaths = listOf("path1", "path2")
        val result = jcrMediaRepositoryImpl.listAllFilePaths()

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).extractFilePaths(queryResult)

            assertEquals(expectedFilePaths, result)
        }
    }

    @Test
    fun getAll() {
        val expectedQuery = """
                SELECT file.*
                FROM [media:file] AS file
            """.trimIndent()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.getNodesFromResult(queryResult)).thenReturn(listOf((node)))
        `when`(fileResourceMapper.copyJcrNodesToFileResource(node)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.getAll()

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).getNodesFromResult(queryResult)
            verify(fileResourceMapper).copyJcrNodesToFileResource(node)

            assertEquals(binaryFileResource, result[0])
        }
    }

    @Test
    fun getByMimeType() {
        val expectedQuery = """
                SELECT file.*
                FROM [media:file] AS file
                INNER JOIN [media:resource] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[jcr:mimeType] = image/jpeg
            """.trimIndent()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.getNodesFromResult(queryResult)).thenReturn(listOf((node)))
        `when`(fileResourceMapper.copyJcrNodesToFileResource(node)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.getByMimeType(MimeType.JPEG)

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).getNodesFromResult(queryResult)
            verify(fileResourceMapper).copyJcrNodesToFileResource(node)

            assertEquals(binaryFileResource, result[0])
        }
    }

    @Test
    fun listFilePathsByMimeType() {
        val expectedQuery = """
                SELECT file.[jcr:path] AS filePath
                FROM [media:file] AS file
                INNER JOIN [media:resource] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[jcr:mimeType] = image/jpeg
            """.trimIndent()

        val expectedFilePaths = listOf("path1", "path2")

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.extractFilePaths(queryResult)).thenReturn(expectedFilePaths)

        val result = jcrMediaRepositoryImpl.listFilePathsByMimeType(MimeType.JPEG)

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).extractFilePaths(queryResult)

            assertEquals(expectedFilePaths, result)
        }
    }

    @Test
    fun getByCategory() {
        val expectedQuery = """
                SELECT file.*
                FROM [media:file] AS file
                INNER JOIN [media:category] AS category ON ISCHILDNODE(file, category)
                WHERE category.[jcr:name] = image
            """.trimIndent()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.getNodesFromResult(queryResult)).thenReturn(listOf((node)))
        `when`(fileResourceMapper.copyJcrNodesToFileResource(node)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.getByCategory(CategoryType.IMAGE)

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).getNodesFromResult(queryResult)
            verify(fileResourceMapper).copyJcrNodesToFileResource(node)

            assertEquals(binaryFileResource, result[0])
        }
    }

    @Test
    fun getByTag() {
        val expectedQuery = """
                SELECT file.*
                FROM [media:file] AS file
                INNER JOIN [media:resource] AS resource ON ISCHILDNODE(resource, file)
                WHERE resource.[media:tags] = 'tagName'
            """.trimIndent()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.getNodesFromResult(queryResult)).thenReturn(listOf((node)))
        `when`(fileResourceMapper.copyJcrNodesToFileResource(node)).thenReturn(binaryFileResource)

        val result = jcrMediaRepositoryImpl.getByTag("tagName")

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).getNodesFromResult(queryResult)
            verify(fileResourceMapper).copyJcrNodesToFileResource(node)

            assertEquals(binaryFileResource, result[0])
        }
    }

    @Test
    fun listFilePathsByCategory() {
        val expectedQuery = """
                SELECT file.[jcr:path] AS filePath
                FROM [media:file] AS file
                INNER JOIN [media:category] AS category ON ISCHILDNODE(file, category)
                WHERE category.[jcr:name] = image
            """.trimIndent()

        val expectedFilePaths = listOf("path1", "path2")

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(jcrQueryExecutor.executeQuery(session, expectedQuery)).thenReturn(queryResult)
        `when`(jcrQueryResultInterpreter.extractFilePaths(queryResult)).thenReturn(expectedFilePaths)

        val result = jcrMediaRepositoryImpl.listFilePathsByCategory(CategoryType.IMAGE)

        softAssert {
            verify(jcrQueryExecutor).executeQuery(session, expectedQuery)
            verify(jcrQueryResultInterpreter).extractFilePaths(queryResult)

            assertEquals(expectedFilePaths, result)
        }
    }

    @Test
    fun create() {
        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        val typeNode = mock<Node>()
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(typeNode)
        `when`(typeNode.hasNode("test")).thenReturn(false)
        `when`(fileResourceMapper.copyFileResourceToJcrNodes(binaryFileResource, typeNode)).thenReturn(fileNode)

        jcrMediaRepositoryImpl.create(binaryFileResource)

        softAssert {
            verify(typeNode).hasNode("test")
            verify(fileResourceMapper).copyFileResourceToJcrNodes(binaryFileResource, typeNode)
        }
    }

    @Test
    fun createThrowsExistsException() {
        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        val typeNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(typeNode)
        `when`(typeNode.hasNode("test")).thenReturn(true)

        assertThrows(ResourceAlreadyExistsException::class.java) {
            jcrMediaRepositoryImpl.create(binaryFileResource)
        }

        verify(typeNode).hasNode("test")
    }

    @Test
    fun createThrowsExceptionOnFail() {
        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(null)

        assertThrows(ResourceNotFoundException::class.java) {
            jcrMediaRepositoryImpl.create(binaryFileResource)
        }

        verify(session).getNode("/media/image/jpeg")
    }

    @Test
    fun createOrReplaceDoesCreate() {
        val filePath = JcrMediaNodePathAssembler.getFileNodePath(binaryFileResource)

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(filePath))).thenThrow(ResourceNotFoundException(""))

        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        val typeNode = mock<Node>()
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(typeNode)
        `when`(typeNode.hasNode("test")).thenReturn(false)
        `when`(fileResourceMapper.copyFileResourceToJcrNodes(binaryFileResource, typeNode)).thenReturn(fileNode)

        jcrMediaRepositoryImpl.createOrReplace(binaryFileResource)

        softAssert {
            verify(typeNode).hasNode("test")
            verify(fileResourceMapper).copyFileResourceToJcrNodes(binaryFileResource, typeNode)
        }
    }

    @Test
    fun createOrReplaceDoesReplace() {
        val filePath = JcrMediaNodePathAssembler.getFileNodePath(binaryFileResource)
        val typeNodePath = JcrMediaNodePathAssembler.getTypePath(binaryFileResource)

        val typeNode = mock<Node>()
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(filePath))).thenReturn(fileNode)

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.getNode(eq(typeNodePath))).thenReturn(typeNode)
        `when`(typeNode.hasNode("test")).thenReturn(false)
        `when`(fileResourceMapper.copyFileResourceToJcrNodes(binaryFileResource, typeNode)).thenReturn(fileNode)

        jcrMediaRepositoryImpl.createOrReplace(binaryFileResource)

        softAssert {
            verify(typeNode).hasNode("test")
            verify(fileNode).remove()
            verify(fileResourceMapper).copyFileResourceToJcrNodes(binaryFileResource, typeNode)
        }
    }

    @Test
    fun delete() {
        val filePath = JcrMediaNodePathAssembler.getFileNodePath(binaryFileResource)
        val fileNode = mock<Node>()

        `when`(jcrSessionWrapperFactory.createOakSessionWrapper()).thenReturn(sessionWrapper)
        `when`(sessionWrapper.getSession()).thenReturn(session)
        `when`(session.nodeExists(eq(filePath))).thenReturn(true)
        `when`(session.getNode(eq(filePath))).thenReturn(fileNode)

        jcrMediaRepositoryImpl.delete(MimeType.JPEG, "test")
        softAssert {
            verify(session).getNode(filePath)
            verify(fileNode).remove()
        }
    }
}
