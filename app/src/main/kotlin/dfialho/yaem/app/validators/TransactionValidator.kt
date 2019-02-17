package dfialho.yaem.app.validators

import dfialho.yaem.app.Transaction

class TransactionValidator(val idValidator: IDValidator) : Validator<Transaction> {

    override fun validate(item: Transaction): List<ValidationError> {
        val errors = idValidator.validate(item.id).toMutableList()

        if (item.incomingAccount == item.sendingAccount) {
            errors += ValidationError.LedgerCommonAccounts(item.incomingAccount)
        }

        return errors
    }
}
