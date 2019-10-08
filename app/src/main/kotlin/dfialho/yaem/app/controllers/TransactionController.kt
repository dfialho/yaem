package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError

class TransactionController(
    repository: TransactionRepository,
    validator: TransactionValidator
) : AbstractResourceController<Transaction>(
    repository,
    validator,
    ValidationError.Transactions::NotFound
) {
    override fun copyWithID(resource: Transaction, newID: ID): Transaction = resource.copy(id = newID)

    override fun create(resource: Transaction): Transaction {
        return try {
            super.create(resource)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingDependency() }
        }
    }

    override fun update(resource: Transaction): Transaction {
        return try {
            super.update(resource)
        } catch (e: ParentMissingException) {
            throwError { ValidationError.Transactions.MissingDependency() }
        }
    }
}