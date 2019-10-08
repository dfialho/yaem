package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Account

class AccountValidator : Validator<Account> {

    override fun validate(item: Account): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.name.length > Account.NAME_MAX_LENGTH) {
            errors += ValidationError.Accounts.Name.TooLong(item.name)
        }

        if (item.name.isBlank()) {
            errors += ValidationError.Accounts.Name.Blank(item.name)
        }

        return errors
    }
}