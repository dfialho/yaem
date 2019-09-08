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
) {
    companion object {
        const val RESOURCE_NAME = "account"
    }

    fun create(account: Account): Account {
        // Generate the ID for the account internally to ensure
        // the IDs are controlled internally
        val uniqueAccount = account.copy(id = randomID())

        throwIfValidationError(validator.validate(uniqueAccount))

        try {
            repository.create(uniqueAccount)
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.AccountNameExists(account.name) }
        }

        return uniqueAccount
    }

    fun get(accountID: ID): Account {
        throwIfValidationError(validateID(accountID))

        try {
            return repository.get(accountID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(RESOURCE_NAME, accountID) }
        }
    }

    fun list(): List<Account> {
        return repository.list()
    }

    fun exists(accountID: ID): Boolean {
        throwIfValidationError(validateID(accountID))
        return repository.exists(accountID)
    }

    fun update(account: Account): Account {
        throwIfValidationError(validator.validate(account))

        try {
            repository.update(account)
            return account
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(RESOURCE_NAME, account.id) }
        } catch (e: DuplicateKeyException) {
            throwError { ValidationError.AccountNameExists(account.name) }
        }
    }

    fun delete(accountID: String) {
        throwIfValidationError(validateID(accountID))

        try {
            repository.delete(accountID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(RESOURCE_NAME, accountID) }
        }
    }
}