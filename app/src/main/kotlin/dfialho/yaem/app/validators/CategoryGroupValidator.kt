package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.CategoryGroup
import dfialho.yaem.app.validators.errors.CategoriesValidationErrors
import dfialho.yaem.app.validators.errors.ValidationError

class CategoryGroupValidator : Validator<CategoryGroup> {

    override fun validate(item: CategoryGroup): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.name.length > Category.NAME_MAX_LENGTH) {
            errors += CategoriesValidationErrors.Name.TooLong(item.name)
        }

        if (item.name.isBlank()) {
            errors += CategoriesValidationErrors.Name.Blank(item.name)
        }

        return errors
    }
}
