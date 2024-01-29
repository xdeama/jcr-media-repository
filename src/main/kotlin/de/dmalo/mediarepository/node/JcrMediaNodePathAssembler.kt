package de.dmalo.mediarepository.node

import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.types.JcrNodeNames
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.model.types.SectionType

internal class JcrMediaNodePathAssembler {

    companion object {
        fun getResourceNodePath(binaryFileResource: BinaryFileResource): String {
            return getResourceNodePath(
                binaryFileResource.getCategoryNodeName(),
                binaryFileResource.getFileExtension(),
                binaryFileResource.fileName
            )
        }

        fun getResourceNodePath(mimeType: MimeType, fileName: String): String {
            return getResourceNodePath(
                mimeType.getCategoryNodeName(),
                mimeType.fileExtension,
                fileName
            )
        }

        fun getResourceNodePath(category: String, fileExtension: String, fileName: String): String {
            return "${getFileNodePath(category, fileExtension, fileName)}/${JcrNodeNames.JCR_CONTENT.key}"
        }

        fun getFileNodePath(binaryFileResource: BinaryFileResource): String {
            return getFileNodePath(
                binaryFileResource.getCategoryNodeName(),
                binaryFileResource.getFileExtension(),
                binaryFileResource.fileName
            )
        }

        fun getFileNodePath(mimeType: MimeType, fileName: String): String {
            return getFileNodePath(mimeType.getCategoryNodeName(), mimeType.fileExtension, fileName)
        }

        fun getFileNodePath(category: String, fileExtension: String, fileName: String): String {
            return "${getTypePath(category, fileExtension)}/$fileName"
        }

        fun getTypePath(binaryFileResource: BinaryFileResource): String {
            return getTypePath(binaryFileResource.getCategoryNodeName(), binaryFileResource.getFileExtension())
        }

        fun getTypePath(category: String, fileExtension: String): String {
            return "${getCategoryPath(category)}/$fileExtension"
        }

        fun getCategoryPath(binaryFileResource: BinaryFileResource): String {
            return getCategoryPath(binaryFileResource.getCategoryNodeName())
        }

        fun getCategoryPath(category: String): String {
            return "/${SectionType.MEDIA.nodeName}/${category}"
        }
    }
}
