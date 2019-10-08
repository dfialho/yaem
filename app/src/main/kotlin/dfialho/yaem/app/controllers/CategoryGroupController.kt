package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.CategoryGroup
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.CategoryGroupRepository
import dfialho.yaem.app.repositories.ChildExistsException
import dfialho.yaem.app.repositories.DuplicateKeyException
import dfialho.yaem.app.validators.CategoryGroupValidator
import dfialho.yaem.app.validators.errors.CategoriesValidationErrors
import dfialho.yaem.app.validators.throwError

class CategoryGroupController(
    repository: CategoryGroupRepository,
    validator: CategoryGroupValidator
) : AbstractResourceController<CategoryGroup>(repository, validator, CategoriesValidationErrors) {

    override fun copyWithID(resource: CategoryGroup, newID: ID): CategoryGroup = resource.copy(id = newID)

    override fun create(resource: CategoryGroup): CategoryGroup {
        return try {
            super.create(resource)
        } catch (e: DuplicateKeyException) {
            throwError { CategoriesValidationErrors.NameExists(resource.name) }
        }
    }

    override fun update(resource: CategoryGroup): CategoryGroup {
        return try {
            super.update(resource)
        } catch (e: DuplicateKeyException) {
            throwError { CategoriesValidationErrors.NameExists(resource.name) }
        }
    }

    override fun delete(id: String) {
        try {
            super.delete(id)
        } catch (e: ChildExistsException) {
            throwError { CategoriesValidationErrors.References(id) }
        }
    }
}