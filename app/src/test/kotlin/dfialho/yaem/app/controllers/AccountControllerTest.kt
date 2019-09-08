package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.uniqueRepositoryManager
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.ValidationError
import org.junit.Test

class AccountControllerTest {

    @Test
    fun `creating an account`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val account = Account("Expense")

        val createdAccount = controller.create(account)

        assertAll {
            assertThat(controller.get(createdAccount.id))
                .isEqualTo(createdAccount)
            assertThat(controller.exists(createdAccount.id))
                .isTrue()
        }
    }

    @Test
    fun `creating an invalid account throws an error and does not create it`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val invalidAccount = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertThat {
            controller.create(invalidAccount)
        }.thrownValidationError()

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
        }.thrownValidationError {
            ValidationError.AccountNameExists(existingAccount.name)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `getting a non-existing account should throw error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.get(nonExistingID)
        }.thrownValidationError {
            ValidationError.NotFound("account", nonExistingID)
        }
    }

    @Test
    fun `getting a non-existing account throws an error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.get(nonExistingID)
        }.thrownValidationError {
            ValidationError.NotFound("account", nonExistingID)
        }
    }

    @Test
    fun `deleting an account removes it from the list`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val otherAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val deletedAccount = controller.create(Account("Deleted"))

        controller.delete(deletedAccount.id)

        assertAll {
            assertThat(controller.list()).containsOnly(*otherAccounts)
            assertThat(controller.exists(deletedAccount.id)).isFalse()
        }
    }

    @Test
    fun `deleting a non-existing account throws an error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.delete(nonExistingID)
        }.thrownValidationError {
            ValidationError.NotFound("account", nonExistingID)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `update an account`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val accountToUpdate = existingAccounts[1].copy(
            name = "New name",
            initialBalance = 1234.5
        )

        val updatedAccount = controller.update(accountToUpdate)

        assertAll {
            assertThat(accountToUpdate).isEqualTo(updatedAccount)
            assertThat(controller.get(accountToUpdate.id)).isEqualTo(accountToUpdate)
        }
    }

    @Test
    fun `updating an account with name too long should throw an error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val originalAccount = existingAccounts[1]
        val accountToUpdate = originalAccount.copy(name = "A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertThat {
            controller.update(accountToUpdate)
        }.thrownValidationError {
            ValidationError.NameTooLong(accountToUpdate.name, ACCOUNT_NAME_MAX_LENGTH)
        }

        assertThat(controller.get(accountToUpdate.id)).isEqualTo(originalAccount)
    }

    @Test
    fun `updating non-existing account should throw an error`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val controller = AccountController(repository, AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val accountToUpdate = Account("Non-Existing", id = randomID())

        assertThat {
            controller.update(accountToUpdate)
        }.thrownValidationError {
            ValidationError.NotFound("account", accountToUpdate.id)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }
}
