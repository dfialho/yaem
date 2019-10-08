package dfialho.yaem.app.controllers

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.ChildExistsException
import dfialho.yaem.app.repositories.DuplicateKeyException
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.errors.AccountsValidationErrors
import dfialho.yaem.app.validators.throwError

class AccountController(
    repository: AccountRepository,
    validator: AccountValidator
) : AbstractResourceController<Account>(repository, validator, AccountsValidationErrors) {

    override fun copyWithID(resource: Account, newID: ID): Account = resource.copy(id = newID)

    override fun create(resource: Account): Account {
        return try {
            super.create(resource)
        } catch (e: DuplicateKeyException) {
            throwError { AccountsValidationErrors.NameExists(resource.name) }
        }
    }

    override fun update(resource: Account): Account {
        return try {
            super.update(resource)
        } catch (e: DuplicateKeyException) {
            throwError { AccountsValidationErrors.NameExists(resource.name) }
        }
    }

    override fun delete(id: String) {
        try {
            super.delete(id)
        } catch (e: ChildExistsException) {
            throwError { AccountsValidationErrors.References(id) }
        }
    }
}