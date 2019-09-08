package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.*

class TransactionController(
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
        throwIfValidationError(validateID(transactionID))
        return repository.get(transactionID) ?: throwError { ValidationError.NotFound(transactionID) }
    }

    fun list(): List<Transaction> {
        return repository.list()
    }

    fun update(trxID: String, trx: Transaction) {
        throwIfValidationError(validateID(trxID))
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
        throwIfValidationError(validateID(transactionID))

        try {
            repository.delete(transactionID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(transactionID) }
        }
    }
}