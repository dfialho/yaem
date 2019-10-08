package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.validators.*

class TransactionController(
    private val repository: TransactionRepository,
    private val validator: TransactionValidator
) : ResourceController<Transaction> {

    override fun create(resource: Transaction): Transaction {
        // Generate the ID for the transaction internally to ensure
        // the IDs are controlled internally
        val transaction = resource.copy(id = randomID())
        throwIfValidationError(validator.validate(transaction))

        try {
            repository.create(transaction)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingAccount() }
        }

        return transaction
    }

    override fun get(id: ID): Transaction {
        throwIfValidationError(validateID(id))

        try {
            return repository.get(id)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(id) }
        }
    }

    override fun list(): List<Transaction> {
        return repository.list()
    }

    fun exists(transactionID: ID): Boolean {
        return repository.exists(transactionID)
    }

    override fun update(resource: Transaction): Transaction {
        throwIfValidationError(validator.validate(resource))

        try {
            repository.update(resource)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(resource.id) }
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingAccount() }
        }

        return resource
    }

    override fun delete(id: String) {
        throwIfValidationError(validateID(id))

        try {
            repository.delete(id)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Transactions.NotFound(id) }
        }
    }
}