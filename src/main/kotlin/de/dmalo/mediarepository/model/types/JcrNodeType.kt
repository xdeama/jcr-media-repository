package de.dmalo.mediarepository.model.types

enum class JcrNodeType(val key: String) {

    // default node types
    NT_RESOURCE("nt:resource"),
    NT_FILE("nt:file"),
    NT_UNSTRUCTURED("nt:unstructured"),

    // section separator
    MEDIA_SECTION("media:section"),

    // custom node types: media storage
    MEDIA_RESOURCE("media:resource"),
    MEDIA_FILE("media:file"),
    MEDIA_CATEGORY("media:category"),
    MEDIA_MIMETYPE("media:mimetype"),

}
