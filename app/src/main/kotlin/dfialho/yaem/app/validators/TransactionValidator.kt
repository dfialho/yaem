package dfialho.yaem.app.validators

import dfialho.yaem.app.Transaction
import dfialho.yaem.app.Transfer

class TransactionValidator(val idValidator: IDValidator) : Validator<Transaction> {

    override fun validate(item: Transaction): List<ValidationError> {
        val errors = idValidator.validate(item.id).toMutableList()

        if (item is Transfer) {
            if (item.incomingAccount == item.outgoingAccount) {
                errors += ValidationError.LedgerCommonAccounts(item.incomingAccount)
            }
        }

        return errors
    }
}
