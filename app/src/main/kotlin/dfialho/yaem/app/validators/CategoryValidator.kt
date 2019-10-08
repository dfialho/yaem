package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.Category

class CategoryValidator : Validator<Category> {

    override fun validate(item: Category): List<ValidationError> {
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

class NameValidator {

    private val errors = mapOf(
        Account is
    )

    fun validate(name: String, maxLength: Int): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (name.length > Category.NAME_MAX_LENGTH) {
            errors += ValidationError.Accounts.Name.TooLong(name)
        }

        if (name.isBlank()) {
            errors += ValidationError.Accounts.Name.Blank(name)
        }

        return errors
    }
}
