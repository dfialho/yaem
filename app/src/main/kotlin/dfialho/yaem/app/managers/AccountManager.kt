package dfialho.yaem.app.managers

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.ChildExistsException
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import dfialho.yaem.app.validators.throwIfValidationError

class AccountManager(
    private val repository: AccountRepository,
    private val validator: AccountValidator
) {

    fun create(account: Account) {
        throwIfValidationError(validator.validate(account))
        repository.create(account)
    }

    fun get(accountID: ID): Account {
        throwIfValidationError(validator.idValidator.validate(accountID))
        return repository.get(accountID) ?: throwError { ValidationError.NotFound(accountID) }
    }

    fun list(): List<Account> {
        return repository.list()
    }

    fun update(accountID: String, account: Account) {
        throwIfValidationError(validator.idValidator.validate(accountID))
        throwIfValidationError(validator.validate(account))

        try {
            repository.update(accountID, account)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(accountID) }
        }
    }

    fun delete(accountID: String) {
        throwIfValidationError(validator.idValidator.validate(accountID))

        try {
            repository.delete(accountID)
        } catch (e: NotFoundException) {
            throwError { ValidationError.NotFound(accountID) }
        } catch (e: ChildExistsException) {
            throwError { ValidationError.AccountReferences(accountID) }
        }
    }
}