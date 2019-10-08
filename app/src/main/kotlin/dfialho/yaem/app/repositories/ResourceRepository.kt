package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.ID

interface ResourceRepository<R> {
    fun create(resource: R)
    fun get(resourceID: ID): R
    fun list(): List<R>
    fun update(resource: R)
    fun delete(resourceID: String)
}