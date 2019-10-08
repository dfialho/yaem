package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Resource
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ResourceRepository
import dfialho.yaem.app.validators.*

abstract class AbstractResourceController<R : Resource>(
    protected val repository: ResourceRepository<R>,
    protected val validator: Validator<R>,
    private val notFoundError: (id: String) -> ValidationError.NotFound
) : ResourceController<R> {

    abstract fun copyWithID(resource: R, newID: ID): R

    override fun create(resource: R): R {
        // Generate the ID for the resource internally to ensure
        // the IDs are controlled internally
        val uniqueResource = copyWithID(resource, randomID())
        throwIfValidationError(validator.validate(uniqueResource))

        repository.create(uniqueResource)

        return uniqueResource
    }

    override fun get(id: ID): R {
        throwIfValidationError(validateID(id))

        try {
            return repository.get(id)
        } catch (e: NotFoundException) {
            throwError { notFoundError(id) }
        }
    }

    override fun list(): List<R> {
        return repository.list()
    }

    override fun update(resource: R): R {
        throwIfValidationError(validator.validate(resource))

        try {
            repository.update(resource)
        } catch (e: NotFoundException) {
            throwError { notFoundError(resource.id) }
        }

        return resource
    }

    override fun delete(id: ID) {
        throwIfValidationError(validateID(id))

        try {
            repository.delete(id)
        } catch (e: NotFoundException) {
            throwError { notFoundError(id) }
        }
    }
}