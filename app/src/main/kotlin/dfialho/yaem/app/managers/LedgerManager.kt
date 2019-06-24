package dfialho.yaem.app.managers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import dfialho.yaem.app.validators.throwIfValidationError

class LedgerManager(
    private val repository: TransactionRepository,
    private val validator: TransactionValidator
) {

    fun create(transaction: Transaction) {
        throwIfValidationError(validator.validate(transaction))

        try {
            repository.create(transaction)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.LedgerMissingAccount() }
        }
    }

    fun get(transactionID: ID): Transaction {
        throwIfValidationError(validator.idValidator.validate(transactionID))
        return repository.get(transactionID) ?: throwError { ValidationError.NotFound(transactionID) }
    }

    fun list(): List<Transaction> {
        return repository.list()
    }

    fun update(trxID: String, trx: Transaction) {
        throwIfValidationError(validator.idValidator.validate(trxID))
        throwIfValidationError(validator.validate(trx))

        try {
            repository.update(trxID, trx)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(trxID) }
        } catch (e: ParentMissingException) {
            throwError { ValidationError.LedgerMissingAccount() }
        }
    }

    fun delete(transactionID: String) {
        throwIfValidationError(validator.idValidator.validate(transactionID))

        try {
            repository.delete(transactionID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(transactionID) }
        }
    }
}