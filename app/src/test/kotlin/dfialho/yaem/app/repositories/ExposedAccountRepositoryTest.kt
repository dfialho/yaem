package dfialho.yaem.app.repositories

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import org.junit.Test
import java.time.Instant

class ExposedAccountRepositoryTest {

    @Test
    fun `create first account`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")

        repository.create(account)

        assertAll {
            assertThat(repository.get(account.id)).isEqualTo(account)
            assertThat(repository.exists(account.id)).isTrue()
            assertThat(repository.list()).containsOnly(account)
        }
    }

    @Test
    fun `after creating N accounts the repository should contain all N accounts`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val accounts = (1..3)
            .map { Account("Acc-$it") }
            .toTypedArray()

        accounts.forEach { repository.create(it) }

        assertThat(repository.list()).containsOnly(*accounts)
    }

    @Test
    fun `creating an account with an existing ID should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")
        repository.create(account)

        assertThat {
            repository.create(account)
        }.thrownError {
            isInstanceOf(DuplicateKeyException::class)
        }
    }

    @Test
    fun `creating an account with an existing name should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val existingAccount = Account("My Account")
        repository.create(existingAccount)

        assertThat {
            repository.create(Account(existingAccount.name))
        }.thrownError {
            isInstanceOf(DuplicateKeyException::class)
        }
    }

    @Test
    fun `get account before creating any should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val nonExistingAccountID = randomID()

        assertThat {
            repository.get(nonExistingAccountID)
        }.thrownError {
            isInstanceOf(NotFoundException::class)
        }
    }

    @Test
    fun `get non-existing account should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        (1..5)
            .map { Account("Account-$it") }
            .forEach { repository.create(it) }
        val nonExistingAccountID = randomID()

        assertThat {
            repository.get(nonExistingAccountID)
        }.thrownError {
            isInstanceOf(NotFoundException::class)
        }
    }

    @Test
    fun `list accounts before creating any should return an empty list`() {
        val repository = uniqueRepositoryManager().getAccountRepository()

        val obtainedAccounts = repository.list()

        assertThat(obtainedAccounts).isEmpty()
    }

    @Test
    fun `creating an account with name with the max length should succeed`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH))

        repository.create(account)

        assertThat(repository.get(account.id)).isEqualTo(account)
    }

    @Test
    fun `exists should return false when no accounts have been created`() {
        val repository = uniqueRepositoryManager().getAccountRepository()

        assertThat(repository.exists(randomID())).isFalse()
    }

    @Test
    fun `exists should return true when the account exists`() {
        val repository = uniqueRepositoryManager().getAccountRepository()

        val account = Account("Account 2")
        repository.create(Account("Account 1"))
        repository.create(account)
        repository.create(Account("Account 3"))
        repository.create(Account("Account 4"))

        assertThat(repository.exists(account.id)).isTrue()
    }

    @Test
    fun `exists should return false when the account does not exist`() {
        val repository = uniqueRepositoryManager().getAccountRepository()

        val nonExistingID = randomID()
        repository.create(Account("Account 1"))
        repository.create(Account("Account 2"))
        repository.create(Account("Account 3"))
        repository.create(Account("Account 4"))

        assertThat(repository.exists(nonExistingID)).isFalse()
    }

    @Test
    fun `deleting a non-existing account throws exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val nonExistingID = randomID()

        assertThat {
            repository.delete(nonExistingID)
        }.thrownError {
            isInstanceOf(NotFoundException::class)
        }
    }

    @Test
    fun `after deleting an existing account the account is no longer listed`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val deletedAccount = Account("My account")
        val otherAccounts = arrayOf(Account("Acc-1"), Account("Acc-2"), Account("Acc-3"))
        val allAccounts = otherAccounts + deletedAccount
        allAccounts.forEach { repository.create(it) }

        repository.delete(deletedAccount.id)

        assertAll {
            assertThat(repository.exists(deletedAccount.id)).isFalse()
            assertThat(repository.list()).containsOnly(*otherAccounts)
        }
    }

    @Test
    fun `update an account`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val accounts = (1..3).map { Account("Acc-$it") }
        accounts.forEach { repository.create(it) }
        val updatedAccount = accounts[1].copy(
            name = "Updated Account",
            startTimestamp = Instant.ofEpochMilli(129031291230L),
            initialBalance = 10.0
        )

        repository.update(updatedAccount)

        assertThat(repository.get(updatedAccount.id))
            .isEqualTo(updatedAccount)
    }

    @Test
    fun `updating a non-existent account should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val accounts = (1..3).map { Account("Acc-$it") }
        accounts.forEach { repository.create(it) }
        val updatedAccount = Account(
            id = randomID(),
            name = "Updated Account",
            startTimestamp = Instant.ofEpochMilli(129031291230L),
            initialBalance = 10.0
        )

        assertThat {
            repository.update(updatedAccount)
        }.thrownError {
            isInstanceOf(NotFoundException::class)
        }
    }

    @Test
    fun `updating an account's name to an existing name should throw exception`() {
        val repository = uniqueRepositoryManager().getAccountRepository()
        val accounts = (1..3).map { Account("Acc-$it") }
        accounts.forEach { repository.create(it) }
        val updatedAccount = accounts[1].copy(
            name = accounts[0].name,
            startTimestamp = Instant.ofEpochMilli(129031291230L),
            initialBalance = 10.0
        )

        assertThat {
            repository.update(updatedAccount)
        }.thrownError {
            isInstanceOf(DuplicateKeyException::class)
        }
    }
}