package de.dmalo.mediarepository.model.types

import de.dmalo.mediarepository.exception.EncodingTypeNotSupportedException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


enum class EncodingType(val typeString: String, val charset: Charset?) {
    UTF_8("UTF-8", StandardCharsets.UTF_8),
    UTF_16("UTF-16", StandardCharsets.UTF_16);

    override fun toString(): String {
        return typeString
    }

    companion object {
        fun fromString(value: String): EncodingType? {
            if ("NONE" == value) return null

            val matchingEncodingType = entries.firstOrNull {
                it.typeString.trim().lowercase() == value.trim().lowercase()
            }
            return matchingEncodingType ?: throw EncodingTypeNotSupportedException(value)
        }
    }
}
