package dfialho.yaem.app.managers

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.repositories.LedgerRepository
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import dfialho.yaem.app.validators.throwIfValidationError

class LedgerManagerImpl(
    private val repository: LedgerRepository,
    private val validator: TransactionValidator
) : LedgerManager {

    override fun create(transaction: Transaction): Transaction {
        throwIfValidationError(validator.validate(transaction))

        try {
            return repository.create(transaction)
        } catch (e: ParentMissingException) {
            throw ValidationErrorException(ValidationError.LedgerMissingAccount())
        }
    }

    override fun get(transactionID: ID): Transaction? {
        throwIfValidationError(validator.idValidator.validate(transactionID))
        return repository.get(transactionID)
    }

    override fun list(): List<Transaction> {
        return repository.list()
    }
}