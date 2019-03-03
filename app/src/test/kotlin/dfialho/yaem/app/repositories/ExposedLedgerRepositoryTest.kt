package dfialho.yaem.app.repositories

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.Account
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.randomID
import org.junit.Test
import java.time.Instant
import java.util.*

class ExposedLedgerRepositoryTest {

    @Test
    fun `create transaction for existing account should include transaction in ledger`() {
        val repositoryManager = uniqueRepositoryManager()
        val account = createAnAccount(repositoryManager)
        val repository = repositoryManager.getLedgerRepository()
        val transaction = Transaction(
            amount = 10.5,
            description = "bananas",
            incomingAccount = account.id,
            timestamp = Instant.ofEpochMilli(1550395065330),
            id = randomID()
        )

        repository.create(transaction)

        assertAll {
            assertThat(repository.get(transaction.id)).isEqualTo(transaction)
            assertThat(repository.list()).containsOnly(transaction)
            assertThat(repository.exists(transaction.id)).isTrue()
        }
    }

    @Test
    fun `list transactions before adding any should return an empty list`() {
        val repository: LedgerRepository = uniqueRepositoryManager().getLedgerRepository()

        val transactions = repository.list()

        assertThat(transactions).isEmpty()
    }

    @Test
    fun `getting a transaction before adding any should return null`() {
        val repository: LedgerRepository = uniqueRepositoryManager().getLedgerRepository()

        val transaction = repository.get(randomID())

        assertThat(transaction).isNull()
    }

    @Test
    fun `getting non-existing transaction after adding some should return null`() {
        val repositoryManager = uniqueRepositoryManager()
        val account = createAnAccount(repositoryManager)
        val repository = repositoryManager.getLedgerRepository()
        repository.create(Transaction(amount = 10.5, description = "bananas", incomingAccount = account.id))

        val transaction = repository.get(randomID())

        assertThat(transaction).isNull()
    }

    @Test
    fun `creating a transaction for non-existing incoming account should throw ParentMissingException`() {
        val repository: LedgerRepository = uniqueRepositoryManager().getLedgerRepository()
        val nonExistingAccountID = randomID()

        assertThat {
            repository.create(
                Transaction(
                    incomingAccount = nonExistingAccountID,
                    amount = 10.5,
                    description = "bananas"
                )
            )
        }.thrownError {
            isInstanceOf(ParentMissingException::class)
        }
    }

    @Test
    fun `creating a transaction for non-existing sending account should throw ParentMissingException`() {
        val repositoryManager = uniqueRepositoryManager()
        val account = createAnAccount(repositoryManager)
        val repository = repositoryManager.getLedgerRepository()
        val nonExistingAccountID = randomID()

        assertThat {
            repository.create(
                Transaction(
                    sendingAccount = nonExistingAccountID,
                    incomingAccount = account.id,
                    amount = 10.5,
                    description = "bananas"
                )
            )
        }.thrownError {
            isInstanceOf(ParentMissingException::class)
        }
    }

    private fun createAnAccount(repositoryManager: ExposedRepositoryManager): Account {
        val accountRepository = repositoryManager.getAccountRepository()
        val account = Account("account-${UUID.randomUUID().toString().substring(0, 5)}")
        accountRepository.create(account)
        return account
    }
}
