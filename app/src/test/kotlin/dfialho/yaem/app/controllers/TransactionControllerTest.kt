package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*

class TransactionControllerTest {

    lateinit var accountController: AccountController
    lateinit var controller: TransactionController

    @Before
    fun setUp() {
        val repositoryManager = uniqueRepositoryManager()
        accountController = AccountController(repositoryManager.getAccountRepository(), AccountValidator())
        controller = TransactionController(repositoryManager.getTransactionRepository(), TransactionValidator())
    }

    @Test
    fun `create new one-way transaction`() {
        val account = createAccount()
        val trx = Transaction(amount = 10.5, receiver = account.id)

        val createdTrx = controller.create(trx)

        assertAll {
            assertThat(controller.get(createdTrx.id)).isEqualTo(createdTrx)
            assertThat(controller.exists(createdTrx.id)).isTrue()
            assertThat(controller.list()).containsOnly(createdTrx)
        }
    }
    @Test
    fun `create new transfer transaction`() {
        val account1 = createAccount()
        val account2 = createAccount()
        val transaction = Transaction(amount = 10.5, receiver = account1.id, sender = account2.id)

        controller.create(transaction)

        assertAll {
            assertThat(controller.get(transaction.id)).isEqualTo(transaction)
            assertThat(controller.exists(transaction.id)).isTrue()
            assertThat(controller.list()).containsOnly(transaction)
        }
    }

    @Test
    fun `creating a transaction for non-existing account should throw error`() {
        createAccount()
        val nonExistingAccountID = randomID()

        assertThat {
            controller.create(Transaction(receiver = nonExistingAccountID, amount = 10.5))
        }.thrownValidationError {
            ValidationError.TransactionMissingAccount()
        }

        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `creating a transaction for non-existing sender account should throw error`() {
        val account = createAccount()
        val nonExistingAccountID = randomID()

        assertThat {
            controller.create(Transaction(receiver = account.id, sender = nonExistingAccountID, amount = 10.5))
        }.thrownValidationError {
            ValidationError.TransactionMissingAccount()
        }
    }

    @Test
    fun `list transactions before adding any accounts should return an empty list`() {
        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `list transactions before adding any should return an empty list`() {
        createAccount()
        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `getting a transaction before adding any should throw error`() {
        createAccount()
        val transactionID = randomID()

        assertThat {
            controller.get(transactionID)
        }.thrownValidationError {
            ValidationError.NotFound("transaction", transactionID)
        }
    }

    @Test
    fun `getting non-existing transaction should throw an error`() {
        val account = createAccount()
        (1..5)
            .map { Transaction(receiver = account.id, amount = 10.5, description = "trx-$it") }
            .forEach { controller.create(it) }
        val nonExistingID = randomID()

        assertThat {
            controller.get(nonExistingID)
        }.thrownValidationError {
            ValidationError.NotFound("transaction", nonExistingID)
        }
    }

    @Test
    fun `deleting a non-existing transaction throws exception`() {
        createAccount()
        val nonExistingID = randomID()

        assertThat {
            controller.delete(nonExistingID)
        }.thrownValidationError {
            ValidationError.NotFound("transaction", nonExistingID)
        }
    }

    @Test
    fun `after deleting an existing transaction the account is no longer listed`() {
        val account = createAccount()
        val otherTransactions = (1..3)
            .map { Transaction(10.0, account.id) }
            .map { controller.create(it) }
            .toTypedArray()
        val deletedTransaction = controller.create(Transaction(50.0, account.id))

        controller.delete(deletedTransaction.id)

        assertAll {
            assertThat(controller.exists(deletedTransaction.id)).isFalse()
            assertThat(controller.list()).containsOnly(*otherTransactions)
        }
    }

    @Test
    fun `creating an invalid transaction throws an error`() {
        val account = createAccount()
        val invalidTransaction = Transaction(receiver = account.id, sender = account.id, amount = 10.5)

        assertThat {
            controller.create(invalidTransaction)
        }

        assertThat(controller.list()).isEmpty()
    }

    @Test
    fun `update a transaction`() {
        val mainAccount = createAccount()
        val secondaryAccount = createAccount()
        val existingTransactions = (1..5)
            .map { Transaction(amount = it.toDouble(), receiver = mainAccount.id) }
            .map { controller.create(it) }
            .toTypedArray()

        val trxToUpdate = existingTransactions[1].copy(
            amount = 150.0,
            receiver = secondaryAccount.id,
            sender = mainAccount.id,
            timestamp = Instant.ofEpochMilli(1568455110298)
        )

        val updatedTrx = controller.update(trxToUpdate)

        assertAll {
            assertThat(updatedTrx).isEqualTo(trxToUpdate)
            assertThat(controller.get(trxToUpdate.id)).isEqualTo(trxToUpdate)
        }
    }

    @Test
    fun `updating non-existing transaction should throw an error`() {
        val account = createAccount()
        val existingTransactions = (1..5)
            .map { Transaction(amount = it.toDouble(), receiver = account.id) }
            .map { controller.create(it) }
            .toTypedArray()

        val trxToUpdate = Transaction(
            id = randomID(),
            amount = 150.0,
            receiver = account.id,
            description = "Non-Existing"
        )

        assertThat {
            controller.update(trxToUpdate)
        }.thrownValidationError {
            ValidationError.NotFound("transaction", trxToUpdate.id)
        }

        assertThat(controller.list()).containsOnly(*existingTransactions)
    }

    @Test
    fun `updating transaction's receiver to non-existing account should throw an error`() {
        val account = createAccount()
        val existingTransactions = (1..5)
            .map { Transaction(amount = it.toDouble(), receiver = account.id) }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingAccountID = randomID()
        val trxToUpdate = existingTransactions[1].copy(receiver = randomID())

        assertThat {
            controller.update(trxToUpdate)
        }.thrownValidationError {
            ValidationError.TransactionMissingAccount()
        }
    }

    @Test
    fun `updating transaction's sender to non-existing account should throw an error`() {
        val account = createAccount()
        val existingTransactions = (1..5)
            .map { Transaction(amount = it.toDouble(), receiver = account.id) }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingAccountID = randomID()
        val trxToUpdate = existingTransactions[1].copy(sender = randomID())

        assertThat {
            controller.update(trxToUpdate)
        }.thrownValidationError {
            ValidationError.TransactionMissingAccount()
        }
    }

    private fun createAccount(): Account {
        val account = Account("Acc-${UUID.randomUUID().toString().substring(0, 5)}")
        return accountController.create(account)
    }
}