package sh.nemo.kjob.repository.mongo.structure

internal enum class LockStructure(val key: String) {
    ID("_id"),
    UPDATED_AT("updated_at")
}
