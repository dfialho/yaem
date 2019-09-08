package dfialho.yaem.app.controllers

import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import org.junit.Test

class AccountControllerTest {

    @Test
    fun `creating a valid account succeeds`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val validator = AccountValidator()
        val controller = AccountController(repository, validator)
        val account = Account("Valid Account")

        val createdAccount = controller.create(account)

        assertThat(controller.get(createdAccount.id))
            .isEqualTo(createdAccount)
    }

    @Test
    fun `creating an invalid account throws an error and does not create it`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val invalidAccount = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertThat {
            controller.create(invalidAccount)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `creating an account with an existing name throws an error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val existingAccount = existingAccounts[1]

        assertThat {
            controller.create(Account(existingAccount.name, id = randomID()))
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            transform { (it as ValidationErrorException).errors }
                .contains(ValidationError.AccountNameExists(existingAccount.name))
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `deleting an existing account removes it from the list`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val validator = AccountValidator()
        val controller = AccountController(repository, validator)
        val otherAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val deletedAccount = controller.create(Account("Deleted"))

        controller.delete(deletedAccount.id)

        assertThat(controller.list()).containsOnly(*otherAccounts)
    }
}
