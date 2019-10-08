package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.ChildExistsException
import dfialho.yaem.app.repositories.DuplicateKeyException
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.validators.*

class AccountController(
    private val repository: AccountRepository,
    private val validator: AccountValidator
) : ResourceController<Account> {

    override fun create(resource: Account): Account {
        // Generate the ID for the account internally to ensure
        // the IDs are controlled internally
        val uniqueAccount = resource.copy(id = randomID())
        throwIfValidationError(validator.validate(uniqueAccount))

        try {
            repository.create(uniqueAccount)
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.Accounts.NameExists(resource.name) }
        }

        return uniqueAccount
    }

    override fun get(id: ID): Account {
        throwIfValidationError(validateID(id))

        try {
            return repository.get(id)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Accounts.NotFound(id) }
        }
    }

    override fun list(): List<Account> {
        return repository.list()
    }

    fun exists(accountID: ID): Boolean {
        throwIfValidationError(validateID(accountID))
        return repository.exists(accountID)
    }

    override fun update(resource: Account): Account {
        throwIfValidationError(validator.validate(resource))

        try {
            repository.update(resource)
            return resource
        } catch (e: NotFoundException) {
            throwError { ValidationError.Accounts.NotFound(resource.id) }
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.Accounts.NameExists(resource.name) }
        }
    }

    override fun delete(id: String) {
        throwIfValidationError(validateID(id))

        try {
            repository.delete(id)
        } catch (e: NotFoundException) {
            throwError { ValidationError.Accounts.NotFound(id) }
        } catch (e: ChildExistsException) {
            throwError { ValidationError.Accounts.References(id) }
        }
    }
}