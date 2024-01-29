package de.dmalo.mediarepository.model.types

enum class JcrNodeProperty(val key: String) {

    // default property types
    JCR_PATH("jcr:path"),
    JCR_DATA("jcr:data"),
    JCR_ENCODING("jcr:encoding"),
    JCR_MIMETYPE("jcr:mimeType"),
    JCR_LAST_MODIFIED("jcr:lastModified"),
    JCR_LAST_MODIFIED_BY("jcr:lastModifiedBy"),
    JCR_NODENAME("jcr:name"),
    JCR_CREATED("jcr:created"),
    JCR_PRIMARY_TYPE("jcr:primaryType"),
    JCR_UUID("jcr:uuid"),

    //custom property types for media:file
    CREATED_BY("media:createdBy"),
    TAGS("media:tags"),
    FILE_SIZE("media:filesize"),

}
