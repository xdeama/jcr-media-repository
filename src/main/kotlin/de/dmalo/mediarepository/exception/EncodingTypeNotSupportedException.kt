package de.dmalo.mediarepository.exception

class EncodingTypeNotSupportedException(encodingType: String) : RuntimeException() {
    override val message: String = "EncodingType '$encodingType' is not supported."
}
