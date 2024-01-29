package de.dmalo.mediarepository.model

import de.dmalo.common.formatter.CalendarFormatter
import de.dmalo.mediarepository.model.types.EncodingType
import de.dmalo.mediarepository.model.types.MimeType
import java.io.ByteArrayInputStream
import java.util.*

class BinaryFileResourceImpl(
    override val data: ByteArrayInputStream,
    override val fileName: String,
    override val mimeType: MimeType,
    override val binaryEncoding: EncodingType?,
    override val fileSizeInBytes: Int,
    override val tags: List<String>,
    override val createdByUser: String,
    override val createdDate: Calendar,
    override val lastModifiedByUser: String,
    override val lastModifiedDate: Calendar
) : BinaryFileResource {

    constructor(
        data: ByteArrayInputStream,
        fileName: String,
        mimeType: MimeType,
        binaryEncoding: EncodingType? = null,
        fileSizeInBytes: Int,
        creatorUserName: String,
        tags: List<String> = emptyList(),
    ) : this(
        data,
        fileName,
        mimeType,
        binaryEncoding,
        fileSizeInBytes,
        tags,
        creatorUserName,
        Calendar.getInstance(),
        creatorUserName,
        Calendar.getInstance()
    )

    constructor(
        data: ByteArrayInputStream,
        fileName: String,
        mimeType: String,
        binaryEncoding: String?,
        fileSizeInBytes: Int,
        creatorUserName: String,
        tags: List<String> = emptyList(),
    ) : this(
        data,
        fileName,
        MimeType.fromString(mimeType),
        binaryEncoding?.let { EncodingType.fromString(it) },
        fileSizeInBytes,
        tags,
        creatorUserName,
        Calendar.getInstance(),
        creatorUserName,
        Calendar.getInstance()
    )

    override fun getFileExtension(): String {
        return mimeType.fileExtension
    }

    override fun getMimeTypeNodeName(): String {
        return mimeType.fileExtension
    }

    override fun getCategoryNodeName(): String {
        return mimeType.getCategoryNodeName()
    }

    override fun getMimeTypeTypeString(): String {
        return mimeType.typeString
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryFileResource) return false

        return (fileName == other.fileName &&
                mimeType == other.mimeType &&
                binaryEncoding == other.binaryEncoding &&
                fileSizeInBytes == other.fileSizeInBytes &&
                tags.size == other.tags.size &&
                tags.toSet() == other.tags.toSet() &&
                createdByUser == other.createdByUser &&
                createdDate.timeInMillis == other.createdDate.timeInMillis &&
                lastModifiedByUser == other.lastModifiedByUser &&
                lastModifiedDate.timeInMillis == other.lastModifiedDate.timeInMillis)
    }

    override fun hashCode(): Int {
        return Objects.hash(
            fileName,
            mimeType,
            binaryEncoding,
            fileSizeInBytes,
            tags.sorted(),
            createdByUser,
            createdDate.timeInMillis,
            lastModifiedByUser,
            lastModifiedDate.timeInMillis
        )
    }

    override fun toString(): String {
        val createdDateString = CalendarFormatter.toISO8601(createdDate)
        val lastModifiedDateString = CalendarFormatter.toISO8601(lastModifiedDate)

        return "BinaryFileResource{" +
                "fileName='$fileName', " +
                "mimeType=$mimeType, " +
                "binaryEncoding=$binaryEncoding, " +
                "fileSizeInBytes=$fileSizeInBytes, " +
                "tags=$tags, " +
                "createdByUser='$createdByUser', " +
                "createdDate=$createdDateString, " +
                "lastModifiedByUser='$lastModifiedByUser', " +
                "lastModifiedDate=$lastModifiedDateString" +
                "}"
    }

}
