package de.dmalo.mediarepository.repository

import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.types.CategoryType
import de.dmalo.mediarepository.model.types.MimeType

interface JcrMediaRepository {
    fun has(mimeType: MimeType, resourceName: String): Boolean

    fun get(mimeType: MimeType, resourceName: String): BinaryFileResource

    fun listAllFilePaths(): List<String>

    fun getAll(): List<BinaryFileResource>

    fun listFilePathsByMimeType(mimeType: MimeType): List<String>

    fun getByMimeType(mimeType: MimeType): List<BinaryFileResource>

    fun listFilePathsByCategory(category: CategoryType): List<String>

    fun getByCategory(category: CategoryType): List<BinaryFileResource>

    fun getByTag(tag: String): List<BinaryFileResource>

    fun create(binaryFileResource: BinaryFileResource)

    fun createOrReplace(binaryFileResource: BinaryFileResource)

    fun delete(mimeType: MimeType, resourceName: String)
}
