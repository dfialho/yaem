package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory
import dfialho.yaem.app.repositories.CategoryRepository
import dfialho.yaem.app.repositories.DuplicateKeyException
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.CategoryValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import dfialho.yaem.app.validators.throwIfValidationError

class CategoryController(
    private val repository: CategoryRepository,
    private val validator: CategoryValidator
) {

    fun create(category: Category): Category {
        throwIfValidationError(validator.validate(category))

        try {
            repository.create(category)
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.Categories.Exists(category.name) }
        }

        return category
    }

    fun create(subCategory: SubCategory): SubCategory {
        throwIfValidationError(validator.validate(Category(subCategory.name)))

        try {
            repository.create(subCategory)
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.Categories.Exists(subCategory.category, subCategory.name) }
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Categories.NotFound(subCategory.category) }
        }

        return subCategory
    }

    fun get(category: String): Category {

        try {
            return repository.get(category)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Categories.NotFound(category) }
        }
    }

    fun list(): List<Category> {
        return repository.list()
    }

    fun rename(oldName: String, newName: String): Category {
        val category = Category(newName)
        throwIfValidationError(validator.validate(category))

        try {
            repository.rename(oldName, newName)
            return category
        } catch (e: NotFoundException) {
            throwError { ValidationError.Categories.NotFound(oldName) }
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.Categories.Exists(newName) }
        }
    }

    fun delete(category: String) {

        try {
            repository.delete(category)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Categories.NotFound(category) }
        }
    }

    fun delete(subCategory: SubCategory) {

        try {
            repository.delete(subCategory)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Categories.NotFound(subCategory) }
        }
    }
}