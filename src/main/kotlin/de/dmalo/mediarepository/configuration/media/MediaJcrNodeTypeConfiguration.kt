package de.dmalo.mediarepository.configuration.media

import de.dmalo.mediarepository.configuration.JcrCustomTypeConfiguration
import de.dmalo.mediarepository.model.types.JcrNodeProperty
import de.dmalo.mediarepository.model.types.JcrNodeType
import de.dmalo.mediarepository.session.JcrSessionWrapper
import de.dmalo.mediarepository.springconfig.MediaRepositoryContextProperties
import org.springframework.stereotype.Component
import javax.jcr.PropertyType
import javax.jcr.Repository
import javax.jcr.nodetype.NodeType
import javax.jcr.nodetype.NodeTypeManager
import javax.jcr.nodetype.NodeTypeTemplate
import javax.jcr.nodetype.PropertyDefinitionTemplate

@Component
internal class MediaJcrNodeTypeConfiguration(
    private val config: MediaRepositoryContextProperties
) : JcrCustomTypeConfiguration {

    override fun initialize(repository: Repository) {
        JcrSessionWrapper(repository, config).use { sessionWrapper ->
            val nodeTypeManager = sessionWrapper.getSession().workspace.nodeTypeManager

            registerCustomFileNodeType(
                nodeTypeManager,
                JcrNodeType.MEDIA_SECTION.key,
                JcrNodeType.NT_UNSTRUCTURED.key
            )
            registerCustomFileNodeType(
                nodeTypeManager,
                JcrNodeType.MEDIA_CATEGORY.key,
                JcrNodeType.NT_UNSTRUCTURED.key
            )
            registerCustomFileNodeType(
                nodeTypeManager,
                JcrNodeType.MEDIA_MIMETYPE.key,
                JcrNodeType.NT_UNSTRUCTURED.key
            )
            registerCustomFileNodeType(nodeTypeManager, JcrNodeType.MEDIA_FILE.key, JcrNodeType.NT_FILE.key)

            configureResourceNodeType(nodeTypeManager)
        }
    }

    private fun configureResourceNodeType(nodeTypeManager: NodeTypeManager): NodeType? {
        val customResourceNodeType: NodeTypeTemplate = createNodeTypeTemplate(
            nodeTypeManager,
            JcrNodeType.MEDIA_RESOURCE.key,
            JcrNodeType.NT_RESOURCE.key
        )

        val createdBy: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.CREATED_BY.key
            requiredType = PropertyType.STRING
            isFullTextSearchable = true
            isMandatory = true
        }

        val createdDate: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.JCR_CREATED.key
            requiredType = PropertyType.DATE
            isMandatory = true
        }

        val lastModifiedBy: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.JCR_LAST_MODIFIED_BY.key
            requiredType = PropertyType.STRING
            isMandatory = true
        }

        val lastModified: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.JCR_LAST_MODIFIED.key
            requiredType = PropertyType.DATE
            isMandatory = true
        }

        val fileSize: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.FILE_SIZE.key
            requiredType = PropertyType.LONG
            isMandatory = true
        }

        val tags: PropertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate().apply {
            name = JcrNodeProperty.TAGS.key
            requiredType = PropertyType.STRING
            isMultiple = true
            isFullTextSearchable = true
            isMandatory = false
        }

        customResourceNodeType.propertyDefinitionTemplates.addAll(
            listOf(createdBy, createdDate, lastModifiedBy, lastModified, fileSize, tags)
        )

        return nodeTypeManager.registerNodeType(customResourceNodeType, true)
    }

    private fun registerCustomFileNodeType(
        nodeTypeManager: NodeTypeManager,
        typeName: String,
        superTypeName: String
    ) {
        val template = createNodeTypeTemplate(nodeTypeManager, typeName, superTypeName)
        nodeTypeManager.registerNodeType(template, true)
    }

    private fun createNodeTypeTemplate(
        nodeTypeManager: NodeTypeManager,
        typeName: String,
        superTypeName: String
    ): NodeTypeTemplate {
        val template: NodeTypeTemplate = nodeTypeManager.createNodeTypeTemplate()
        template.name = typeName
        template.setDeclaredSuperTypeNames(arrayOf(superTypeName))
        return template
    }
}
