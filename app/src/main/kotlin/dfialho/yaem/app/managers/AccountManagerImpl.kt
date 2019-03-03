package dfialho.yaem.app.managers

import dfialho.yaem.app.Account
import dfialho.yaem.app.ID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.throwIfValidationError

class AccountManagerImpl(
    private val repository: AccountRepository,
    private val validator: AccountValidator
) : AccountManager {

    override fun create(account: Account) {
        throwIfValidationError(validator.validate(account))
        repository.create(account)
    }

    override fun get(accountID: ID): Account? {
        throwIfValidationError(validator.idValidator.validate(accountID))
        return repository.get(accountID)
    }

    override fun list(): List<Account> {
        return repository.list()
    }

    override fun delete(accountID: String) {
        throwIfValidationError(validator.idValidator.validate(accountID))
        repository.delete(accountID)
    }
}