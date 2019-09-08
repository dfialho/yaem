package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.ChildExistsException
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.validators.*

class AccountController(
    private val repository: AccountRepository,
    private val validator: AccountValidator
) {

    fun create(account: Account) {
        throwIfValidationError(validator.validate(account))
        repository.create(account)
    }

    fun get(accountID: ID): Account {
        throwIfValidationError(validateID(accountID))

        try {
            return repository.get(accountID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(accountID) }
        }
    }

    fun list(): List<Account> {
        return repository.list()
    }

    fun update(accountID: String, account: Account) {
        throwIfValidationError(validateID(accountID))
        throwIfValidationError(validator.validate(account))

        try {
            // FIXME remove account ID parameter
            repository.update(account.copy(id = accountID))
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(accountID) }
        }
    }

    fun delete(accountID: String) {
        throwIfValidationError(validateID(accountID))

        try {
            repository.delete(accountID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(accountID) }
        } catch (e: ChildExistsException) {
            throwError { ValidationError.AccountReferences(accountID) }
        }
    }
}