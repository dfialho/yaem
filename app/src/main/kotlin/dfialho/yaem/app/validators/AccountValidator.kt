package dfialho.yaem.app.validators

import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account

class AccountValidator(val idValidator: IDValidator) : Validator<Account> {

    override fun validate(item: Account): List<ValidationError> {
        val errors = idValidator.validate(item.id).toMutableList()

        if (item.name.length > ACCOUNT_NAME_MAX_LENGTH) {
            errors += ValidationError.NameTooLong(item.name, ACCOUNT_NAME_MAX_LENGTH)
        }

        return errors
    }
}