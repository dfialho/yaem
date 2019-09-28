package dfialho.yaem.app.validators

import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account

class AccountValidator : Validator<Account> {

    override fun validate(item: Account): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.name.length > ACCOUNT_NAME_MAX_LENGTH) {
            errors += ValidationError.NameTooLong(item.name, ACCOUNT_NAME_MAX_LENGTH)
        }

        if (item.name.isBlank()) {
            errors += ValidationError.NameIsBlank()
        }

        return errors
    }
}