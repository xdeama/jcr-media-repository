package de.dmalo.mediarepository.model

import de.dmalo.mediarepository.model.types.EncodingType
import de.dmalo.mediarepository.model.types.MimeType
import java.io.ByteArrayInputStream
import java.util.*

interface BinaryFileResource {
    val data: ByteArrayInputStream
    val fileName: String
    val mimeType: MimeType
    val binaryEncoding: EncodingType?
    val fileSizeInBytes: Int
    val tags: List<String>
    val createdByUser: String
    val createdDate: Calendar
    val lastModifiedByUser: String
    val lastModifiedDate: Calendar

    fun getFileExtension(): String

    fun getMimeTypeNodeName(): String

    fun getCategoryNodeName(): String

    fun getMimeTypeTypeString(): String

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}
