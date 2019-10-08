package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.testutils.resources.anyAccount
import dfialho.yaem.app.testutils.resources.anyTransaction
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import io.kotlintest.specs.StringSpec
import java.time.Instant

class TransactionControllerTest : StringSpec({

    resourceControllerTests<Transaction>() {
        val manager = uniqueRepositoryManager()
        val accountController = AccountController(manager.getAccountRepository(), AccountValidator())
        val account = accountController.create(Account("Checking Account"))

        controller = TransactionController(manager.getTransactionRepository(), TransactionValidator())
        anyResource = { anyTransaction(account.id) }
        invalidResource = { Transaction(10.0, receiver = account.id, sender = account.id) }
        copy = { id -> copy(id = id) }
        update = { copy(amount = 150.0, receiver = account.id, timestamp = Instant.now()) }
        notFoundError = ValidationError.Transactions::NotFound
    }

    subResourceControllerTests<Transaction, Account>(parentName = "Receiver Account") {
        val manager = uniqueRepositoryManager()

        parentController = AccountController(manager.getAccountRepository(), AccountValidator())
        controller = TransactionController(manager.getTransactionRepository(), TransactionValidator())
        anyParent = { anyAccount() }
        anyResource = { accountId -> anyTransaction(accountId) }
        update = { accountId -> copy(amount = 150.0, receiver = accountId, timestamp = Instant.now()) }
        parentMissingError = { ValidationError.Transactions.MissingDependency() }
        referencesError = ValidationError.Accounts::References
    }

    subResourceControllerTests<Transaction, Account>(parentName = "Sender Account") {
        val manager = uniqueRepositoryManager()
        val accountController = AccountController(manager.getAccountRepository(), AccountValidator())
        val receiverAccount = accountController.create(anyAccount())

        parentController = accountController
        controller = TransactionController(manager.getTransactionRepository(), TransactionValidator())
        anyParent = { anyAccount() }
        anyResource = { accountId -> anyTransaction(receiverAccount.id, accountId) }
        update = { accountId -> copy(amount = 150.0, sender = accountId, timestamp = Instant.now()) }
        parentMissingError = { ValidationError.Transactions.MissingDependency() }
        referencesError = ValidationError.Accounts::References
    }

    "create a transfer $Transaction" {
        val manager = uniqueRepositoryManager()
        val accountController = AccountController(manager.getAccountRepository(), AccountValidator())
        val controller = TransactionController(manager.getTransactionRepository(), TransactionValidator())
        val account1 = accountController.create(anyAccount())
        val account2 = accountController.create(anyAccount())

        val createdTransaction = controller.create(Transaction(
            amount = 10.5,
            receiver = account1.id,
            sender = account2.id
        ))

        assertAll {
            assertThat(controller.get(createdTransaction.id))
                .isEqualTo(createdTransaction)
            assertThat(controller.list())
                .containsOnly(createdTransaction)
        }
    }
})
