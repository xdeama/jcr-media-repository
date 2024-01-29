package de.dmalo.business

import de.dmalo.mediarepository.model.BinaryFileResource
import de.dmalo.mediarepository.model.types.CategoryType
import de.dmalo.mediarepository.model.types.MimeType
import de.dmalo.mediarepository.repository.JcrMediaRepository
import org.springframework.stereotype.Service

@Service
class JcrMediaService(private val jcrMediaRepository: JcrMediaRepository) : MediaService {

    override fun has(mimeType: MimeType, resourceName: String): Boolean {
        return jcrMediaRepository.has(mimeType, resourceName)
    }

    override fun get(mimeType: MimeType, resourceName: String): BinaryFileResource {
        return jcrMediaRepository.get(mimeType, resourceName)
    }

    override fun listAllFilePaths(): List<String> {
        return jcrMediaRepository.listAllFilePaths()
    }

    override fun getAll(): List<BinaryFileResource> {
        return jcrMediaRepository.getAll()
    }

    override fun getByMimeType(mimeType: MimeType): List<BinaryFileResource> {
        return jcrMediaRepository.getByMimeType(mimeType)
    }

    override fun listFilePathsByMimeType(mimeType: MimeType): List<String> {
        return jcrMediaRepository.listFilePathsByMimeType(mimeType)
    }

    override fun getByCategory(category: CategoryType): List<BinaryFileResource> {
        return jcrMediaRepository.getByCategory(category)
    }

    override fun listFilePathsByCategory(category: CategoryType): List<String> {
        return jcrMediaRepository.listFilePathsByCategory(category)
    }

    override fun getByTag(tag: String): List<BinaryFileResource> {
        return jcrMediaRepository.getByTag(tag)
    }

    override fun create(binaryFileResource: BinaryFileResource) {
        jcrMediaRepository.create(binaryFileResource)
    }

    override fun createOrReplace(binaryFileResource: BinaryFileResource) {
        jcrMediaRepository.createOrReplace(binaryFileResource)
    }

    override fun delete(mimeType: MimeType, resourceName: String) {
        jcrMediaRepository.delete(mimeType, resourceName)
    }
}
