package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Transaction

class TransactionValidator : Validator<Transaction> {

    override fun validate(item: Transaction): List<ValidationError> {
        val errors = validateID(item.id).toMutableList()

        if (item.receiver == item.sender) {
            errors += ValidationError.TransactionCommonAccounts(item.receiver)
        }

        return errors
    }
}
