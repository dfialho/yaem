package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Transaction

class TransactionValidator(val idValidator: IDValidator) : Validator<Transaction> {

    override fun validate(item: Transaction): List<ValidationError> {
        val errors = idValidator.validate(item.id).toMutableList()

        if (item.receiver == item.sender) {
            errors += ValidationError.LedgerCommonAccounts(item.receiver)
        }

        return errors
    }
}
