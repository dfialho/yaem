package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.validators.errors.TransactionsValidationErrors
import dfialho.yaem.app.validators.errors.ValidationError

class TransactionValidator : Validator<Transaction> {

    override fun validate(item: Transaction): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.receiver == item.sender) {
            errors += TransactionsValidationErrors.CommonAccounts(item.receiver)
        }

        return errors
    }
}
