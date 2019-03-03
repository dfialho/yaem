package dfialho.yaem.app.managers

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.repositories.LedgerRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import dfialho.yaem.app.validators.throwIfValidationError

class LedgerManagerImpl(
    private val repository: LedgerRepository,
    private val validator: TransactionValidator
) : LedgerManager {

    override fun create(transaction: Transaction) {
        throwIfValidationError(validator.validate(transaction))

        try {
            repository.create(transaction)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.LedgerMissingAccount() }
        }
    }

    override fun get(transactionID: ID): Transaction {
        throwIfValidationError(validator.idValidator.validate(transactionID))
        return repository.get(transactionID) ?: throwError { ValidationError.NotFound(transactionID) }
    }

    override fun list(): List<Transaction> {
        return repository.list()
    }

    override fun delete(transactionID: String) {
        throwIfValidationError(validator.idValidator.validate(transactionID))

        try {
            repository.delete(transactionID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(transactionID) }
        }
    }
}