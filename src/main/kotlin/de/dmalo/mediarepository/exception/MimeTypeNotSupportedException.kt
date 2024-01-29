package de.dmalo.mediarepository.exception

class MimeTypeNotSupportedException(mimeType: String) : RuntimeException() {
    override val message: String = "Mimetype '$mimeType' is not supported."
}
