package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.validators.*

class TransactionController(
    private val repository: TransactionRepository,
    private val validator: TransactionValidator
) {
    fun create(transaction: Transaction): Transaction {
        throwIfValidationError(validator.validate(transaction))

        try {
            repository.create(transaction)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingAccount() }
        }

        return transaction
    }

    fun get(transactionID: ID): Transaction {
        throwIfValidationError(validateID(transactionID))

        try {
            return repository.get(transactionID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(transactionID) }
        }
    }

    fun list(): List<Transaction> {
        return repository.list()
    }

    fun exists(transactionID: ID): Boolean {
        return repository.exists(transactionID)
    }

    fun update(trx: Transaction): Transaction {
        throwIfValidationError(validator.validate(trx))

        try {
            repository.update(trx)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(trx.id) }
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingAccount() }
        }

        return trx
    }

    fun delete(transactionID: String) {
        throwIfValidationError(validateID(transactionID))

        try {
            repository.delete(transactionID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(transactionID) }
        }
    }
}