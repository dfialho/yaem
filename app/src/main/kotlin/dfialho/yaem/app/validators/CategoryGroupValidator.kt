package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.CategoryGroup

class CategoryGroupValidator : Validator<CategoryGroup> {

    override fun validate(item: CategoryGroup): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.name.length > Category.NAME_MAX_LENGTH) {
            errors += ValidationError.Accounts.Name.TooLong(item.name)
        }

        if (item.name.isBlank()) {
            errors += ValidationError.Accounts.Name.Blank(item.name)
        }

        return errors
    }
}
