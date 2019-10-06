package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.resources.anyAccount
import dfialho.yaem.app.testutils.resources.anyTransaction
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import io.kotlintest.specs.AnnotationSpec
import java.time.Instant

class AccountControllerTest : AnnotationSpec() {

    lateinit var controller: AccountController
    lateinit var trxController: TransactionController

    @Before
    fun setUp() {
        val manager = uniqueRepositoryManager()
        controller = AccountController(manager.getAccountRepository(), AccountValidator())
        trxController = TransactionController(manager.getTransactionRepository(), TransactionValidator())
    }

    @Test
    fun `creating first account`() {

        val createdAccount = controller.create(Account("Acc"))

        assertAll {
            assertThat(controller.get(createdAccount.id))
                .isEqualTo(createdAccount)
            assertThat(controller.exists(createdAccount.id))
                .isTrue()
        }
    }

    @Test
    fun `after creating N accounts the repository should contain all N accounts`() {

        val createdAccounts = (1..5)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()

        assertThat(controller.list()).containsOnly(*createdAccounts)
    }

    @Test
    fun `creating an invalid account throws an error and does not create it`() {
        val invalidAccount = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertThat {
            controller.create(invalidAccount)
        }.thrownValidationError()

        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `creating an account with an existing ID should succeed`() {
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val existingAccount = existingAccounts.first()

        val createdAccount = controller.create(Account("New Name", id = existingAccount.id))

        assertThat(controller.get(createdAccount.id))
            .isEqualTo(createdAccount)
    }

    @Test
    fun `creating an account with an existing name throws an error`() {
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val existingAccount = existingAccounts[1]

        assertThat {
            controller.create(Account(existingAccount.name, id = randomID()))
        }.thrownValidationError {
            ValidationError.Accounts.NameExists(existingAccount.name)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `creating an account whose name is longer than the max should throw error`() {
        val account = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))


        assertThat {
            controller.create(account)
        }.thrownValidationError {
            ValidationError.Accounts.NameTooLong(account.name, ACCOUNT_NAME_MAX_LENGTH)
        }
    }

    @Test
    fun `creating an account whose name's length is equal to the max should succeed`() {
        val account = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH))

        val createdAccount = controller.create(account)

        assertThat(controller.get(createdAccount.id)).isEqualTo(createdAccount)
    }

    @Test
    fun `creating an account with a blank name should throw an error`() {
        val account = Account("  ")

        assertThat {
            controller.create(account)
        }.thrownValidationError {
            ValidationError.Accounts.NameIsBlank()
        }
    }

    @Test
    fun `getting a non-existing account should throw error`() {
        (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.get(nonExistingID)
        }.thrownValidationError {
            ValidationError.Accounts.NotFound(nonExistingID)
        }
    }

    @Test
    fun `exists should return false when no accounts have been created`() {
        assertThat(controller.exists(randomID())).isFalse()
    }

    @Test
    fun `exists should return true when the account exists`() {

        val accounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }

        assertAll {
            accounts.forEach {
                assertThat(controller.exists(it.id)).isTrue()
            }
        }
    }

    @Test
    fun `listing accounts before creating any should return an empty list`() {
        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `deleting an account removes it from the list`() {
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
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.delete(nonExistingID)
        }.thrownValidationError {
            ValidationError.Accounts.NotFound(nonExistingID)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `deleting an account that is a receiver of a transaction should throw an error`() {
        val account = controller.create(anyAccount())
        val transaction = trxController.create(anyTransaction(account.id))

        assertThat {
            controller.delete(account.id)
        }.thrownValidationError {
            ValidationError.Accounts.References(account.id)
        }

        assertThat(controller.get(account.id)).isEqualTo(account)
        assertThat(trxController.get(transaction.id)).isEqualTo(transaction)
    }

    @Test
    fun `deleting an account that is the sender of a transaction should throw an error`() {
        val mainAccount = controller.create(anyAccount())
        val account = controller.create(anyAccount())
        val transaction = trxController.create(
            anyTransaction(
                account = mainAccount.id,
                sender = account.id
            )
        )

        assertThat {
            controller.delete(account.id)
        }.thrownValidationError {
            ValidationError.Accounts.References(account.id)
        }

        assertThat(controller.get(account.id)).isEqualTo(account)
        assertThat(trxController.get(transaction.id)).isEqualTo(transaction)
    }

    @Test
    fun `update an account`() {
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
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val originalAccount = existingAccounts[1]
        val accountToUpdate = originalAccount.copy(name = "A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertThat {
            controller.update(accountToUpdate)
        }.thrownValidationError {
            ValidationError.Accounts.NameTooLong(accountToUpdate.name, ACCOUNT_NAME_MAX_LENGTH)
        }

        assertThat(controller.get(accountToUpdate.id)).isEqualTo(originalAccount)
    }

    @Test
    fun `updating non-existing account should throw an error`() {
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val accountToUpdate = Account("Non-Existing", id = randomID())

        assertThat {
            controller.update(accountToUpdate)
        }.thrownValidationError {
            ValidationError.Accounts.NotFound(accountToUpdate.id)
        }

        assertThat(controller.list()).containsOnly(*existingAccounts)
    }

    @Test
    fun `updating an account's name to an existing name should throw exception`() {
        val accounts = (1..5)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }

        val updatedAccount = accounts[1].copy(
            name = accounts.first().name,
            startTimestamp = Instant.ofEpochMilli(129031291230L),
            initialBalance = 10.0
        )

        assertThat {
            controller.update(updatedAccount)
        }.thrownValidationError {
            ValidationError.Accounts.NameExists(updatedAccount.name)
        }
    }

    @Test
    fun `account can be deleted after all of its transactions are deleted`() {
        controller.create(anyAccount())
        val account = controller.create(anyAccount())
        (1..5)
            .map { anyTransaction(account.id) }
            .map { trxController.create(it) }
            .forEach { trxController.delete(it.id) }

        controller.delete(account.id)

        assertThat(controller.list()).doesNotContain(account)
    }
}
