package de.dmalo.mediarepository.model.types

import de.dmalo.mediarepository.exception.MimeTypeNotSupportedException


enum class MimeType(val typeString: String, val fileExtension: String, val category: CategoryType) {
    BASE64("application/octet-stream", "B64", CategoryType.UNDEFINED),
    JPEG("image/jpeg", "jpeg", CategoryType.IMAGE),
    PNG("image/png", "png", CategoryType.IMAGE),
    GIF("image/gif", "gif", CategoryType.IMAGE),
    BMP("image/bmp", "bmp", CategoryType.IMAGE),
    TIFF("image/tiff", "tiff", CategoryType.IMAGE),
    SVG("image/svg+xml", "svg", CategoryType.IMAGE),
    WEBP("image/webp", "webp", CategoryType.IMAGE),
    PDF("application/pdf", "pdf", CategoryType.DOCUMENT),
    MP4("video/mp4", "mp4", CategoryType.VIDEO),
    TEXT_PLAIN("text/plain", "txt", CategoryType.DOCUMENT),
    RTF("application/rtf", "rtf", CategoryType.DOCUMENT),
    HTML("text/html", "html", CategoryType.DOCUMENT),
    JSON("application/json", "json", CategoryType.DOCUMENT),
    XML("application/xml", "xml", CategoryType.DOCUMENT);

    fun getCategoryNodeName(): String = this.category.nodeName

    companion object {
        fun fromString(type: String): MimeType {
            val matchingMimeType = entries.firstOrNull {
                it.typeString.trim().lowercase() == type.trim().lowercase()
            }
            return matchingMimeType ?: throw MimeTypeNotSupportedException(type)
        }

        fun allCategoryNodeNames(): List<String> {
            return entries.map { it.category.nodeName }.distinct()
        }
    }
}
