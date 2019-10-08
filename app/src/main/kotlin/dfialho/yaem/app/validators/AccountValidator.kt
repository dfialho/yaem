package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.validators.errors.AccountsValidationErrors
import dfialho.yaem.app.validators.errors.ValidationError

class AccountValidator : Validator<Account> {

    override fun validate(item: Account): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.name.length > Account.NAME_MAX_LENGTH) {
            errors += AccountsValidationErrors.Name.TooLong(item.name)
        }

        if (item.name.isBlank()) {
            errors += AccountsValidationErrors.Name.Blank(item.name)
        }

        return errors
    }
}