package de.dmalo.web.model

import de.dmalo.common.formatter.CalendarFormatter
import de.dmalo.mediarepository.model.BinaryFileResource

data class MediaResourceDTO(
    val payloadURI: String,
    val fileName: String,
    val mimeType: String,
    val binaryEncoding: String,
    val fileSizeInBytes: Int,
    val tags: List<String>,
    val createdByUser: String,
    val createdDate: String,
    val lastModifiedByUser: String,
    val lastModifiedDate: String
) {
    constructor(
        binaryFileResource: BinaryFileResource,
        payloadURI: String
    ) : this(
        payloadURI = payloadURI,
        fileName = binaryFileResource.fileName,
        mimeType = binaryFileResource.mimeType.typeString,
        binaryEncoding = binaryFileResource.binaryEncoding?.typeString ?: "",
        fileSizeInBytes = binaryFileResource.fileSizeInBytes,
        tags = binaryFileResource.tags,
        createdByUser = binaryFileResource.createdByUser,
        createdDate = CalendarFormatter.toISO8601(binaryFileResource.createdDate),
        lastModifiedByUser = binaryFileResource.lastModifiedByUser,
        lastModifiedDate = CalendarFormatter.toISO8601(binaryFileResource.lastModifiedDate)
    )
}

data class MediaResourceDTOWrapperList(val mediaResourceDTOList: List<MediaResourceDTO>)
