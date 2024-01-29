package de.dmalo.mediarepository.mapper

import de.dmalo.mediarepository.model.BinaryFileResource
import javax.jcr.Node

interface FileResourceMapper {
    fun copyJcrNodesToFileResource(fileNode: Node): BinaryFileResource
    fun copyFileResourceToJcrNodes(binaryFileResource: BinaryFileResource, parentNode: Node): Node
}
