package dfialho.yaem.app.controllers

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.resources.anyAccount
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.errors.AccountsValidationErrors
import io.kotlintest.specs.StringSpec
import java.time.Instant

class AccountControllerTest : StringSpec({

    resourceControllerTests<Account>() {
        val manager = uniqueRepositoryManager()

        controller = AccountController(manager.getAccountRepository(), AccountValidator())
        anyResource = { anyAccount() }
        invalidResource = { Account("   ") }
        copy = { id -> copy(id = id) }
        update = { copy(name = "$name-updated", initialBalance = Math.random()) }
        validationErrors = AccountsValidationErrors
    }

    "creating an account with an existing name throws an error" {
        val manager = uniqueRepositoryManager()
        val controller = AccountController(manager.getAccountRepository(), AccountValidator())
        val existingAccounts = (1..3)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val existingAccount = existingAccounts[1]

        assertThat {
            controller.create(Account(existingAccount.name, id = randomID()))
        }.thrownValidationError {
            AccountsValidationErrors.NameExists(existingAccount.name)
        }

        assertThat(controller.list())
            .containsOnly(*existingAccounts)
    }

    "updating an account's name to an existing name should throw exception" {
        val manager = uniqueRepositoryManager()
        val controller = AccountController(manager.getAccountRepository(), AccountValidator())
        val accounts = (1..5)
            .map { Account("Acc-$it") }
            .map { controller.create(it) }
        val originalAccount = accounts.last()
        val updatedAccount = originalAccount.copy(
            name = accounts.first().name,
            startTimestamp = Instant.ofEpochMilli(129031291230L),
            initialBalance = 10.0
        )

        assertThat {
            controller.update(updatedAccount)
        }.thrownValidationError {
            AccountsValidationErrors.NameExists(updatedAccount.name)
        }

        assertThat(controller.get(updatedAccount.id))
            .isEqualTo(originalAccount)
    }
})
