package dfialho.yaem.app.repositories

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.Account
import dfialho.yaem.app.exceptions.FoundException
import dfialho.yaem.app.randomID
import org.junit.Test

class ExposedAccountRepositoryTest {

    @Test
    fun `create first account`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")

        val createdAccount = repository.create(account)

        assertAll {
            assertThat(createdAccount).isEqualTo(account)
            assertThat(repository.get(account.id)).isEqualTo(account)
            assertThat(repository.exists(account.id)).isTrue()
            assertThat(repository.list()).containsOnly(account)
        }
    }

    @Test
    fun `after creating 3 accounts the repository should contain all 3 accounts`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account1 = Account("Account 1")
        val account2 = Account("Account 2")
        val account3 = Account("Account 3")

        val createdAccount1 = repository.create(account1)
        val createdAccount2 = repository.create(account2)
        val createdAccount3 = repository.create(account3)

        assertThat(repository.list()).containsOnly(createdAccount1, createdAccount2, createdAccount3)
    }

    @Test
    fun `creating an account with an existing id should fail`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")
        repository.create(account)

        assertThat {
            repository.create(account)
        }.thrownError {
            isInstanceOf(FoundException::class)
        }
    }

    @Test
    fun `creating an account with an existing name should succeed`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account1 = Account("My Account")
        val account2 = Account("My Account")

        repository.create(account1)
        repository.create(account2)

        assertThat(repository.list()).containsAll(account1, account2)
    }

    @Test
    fun `get account before creating any should return null`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")

        val obtainedAccount = repository.get(account.id)

        assertThat(obtainedAccount).isNull()
    }

    @Test
    fun `get non-existing account should return null`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("My Account")
        repository.create(account)
        val nonExistingAccountID = randomID()

        val obtainedAccount= repository.get(nonExistingAccountID)

        assertThat(obtainedAccount).isNull()
    }

    @Test
    fun `list accounts before creating any should return an empty list`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()

        val obtainedAccounts = repository.list()

        assertThat(obtainedAccounts).isEmpty()
    }

    @Test
    fun `creating an account with name with the max length should succeed`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH))

        repository.create(account)

        assertThat(repository.get(account.id)).isEqualTo(account)
    }

    @Test
    fun `creating an account with name over the max length should fail`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()
        val account = Account("A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        assertAll {
            assertThat { repository.create(account) }.thrownError { }
            assertThat(repository.list()).isEmpty()
        }
    }

    @Test
    fun `exists should return false when no accounts have been created`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()

        assertThat(repository.exists(randomID())).isFalse()
    }

    @Test
    fun `exists should return true when the account exists`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()

        val account = Account("Account 2")
        repository.create(Account("Account 1"))
        repository.create(account)
        repository.create(Account("Account 3"))
        repository.create(Account("Account 4"))

        assertThat(repository.exists(account.id)).isTrue()
    }

    @Test
    fun `exists should return false when the account does not exist`() {
        val repository: AccountRepository = uniqueRepositoryManager().getAccountRepository()

        val nonExistingID = randomID()
        repository.create(Account("Account 1"))
        repository.create(Account("Account 2"))
        repository.create(Account("Account 3"))
        repository.create(Account("Account 4"))

        assertThat(repository.exists(nonExistingID)).isFalse()
    }
}