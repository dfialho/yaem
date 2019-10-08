package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Resource

interface ResourceController<R : Resource> {
    fun create(resource: R): R
    fun get(id: ID): R
    fun list(): List<R>
    fun update(resource: R): R
    fun delete(id: ID)
}
