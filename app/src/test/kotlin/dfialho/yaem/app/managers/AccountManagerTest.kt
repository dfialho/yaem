package dfialho.yaem.app.managers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.Account
import dfialho.yaem.app.randomID
import dfialho.yaem.app.randomTransfer
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.repositories.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.IDValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import io.mockk.*
import org.junit.Test

class AccountManagerTest {

    @Test
    fun `when the account is invalid the manager does not try to create the account`() {
        val repository = mockk<AccountRepository>()
        val validator = mockk<AccountValidator>()
        val manager = AccountManager(repository, validator)
        val account = Account("Invalid Account")

        every { validator.validate(account) } returns listOf(ValidationError.InvalidID(account.id))
        every { repository.create(any()) } just Runs

        assertThat {
            manager.create(account)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `when no validation error occurs the manager tries to create the account`() {
        val repository = mockk<AccountRepository>(relaxed = true)
        val validator = AccountValidator(IDValidator())
        val manager = AccountManager(repository, validator)
        val account = Account("Valid Account")

        manager.create(account)

        verifyAll { repository.create(account) }
    }

    @Test
    fun `when the account ID is invalid the manager does not try to delete any account`() {
        val repository = mockk<AccountRepository>()
        val validator = AccountValidator(IDValidator())
        val manager = AccountManager(repository, validator)
        val accountID = "invalid id"

        every { repository.delete(accountID) } just Runs

        assertThat {
            manager.delete(accountID)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `when the account ID is valid the manager tries to delete the account`() {
        val repository = mockk<AccountRepository>(relaxed = true)
        val validator = AccountValidator(IDValidator())
        val manager = AccountManager(repository, validator)
        val accountID = randomID()

        manager.delete(accountID)

        verifyAll { repository.delete(accountID) }
    }

    @Test
    fun `deleting an account which is incoming account of at least one transaction returns a validation error`() {
        val repositoryManager = uniqueRepositoryManager()
        val accountRepository: AccountRepository = repositoryManager.getAccountRepository()
        val transactionRepository: TransactionRepository = repositoryManager.getLedgerRepository()
        val manager = AccountManager(accountRepository, AccountValidator(IDValidator()))

        val incomingAccount = Account("Incoming")
        val outgoingAccount = Account("Sending")
        accountRepository.create(incomingAccount)
        accountRepository.create(outgoingAccount)
        transactionRepository.create(randomTransfer(incomingAccount.id, outgoingAccount.id))

        assertThat {
            manager.delete(incomingAccount.id)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            containsError(ValidationError.AccountReferences(incomingAccount.id))
        }
    }

    @Test
    fun `deleting an account which is sending account of at least one transaction returns a validation error`() {
        val repositoryManager = uniqueRepositoryManager()
        val accountRepository: AccountRepository = repositoryManager.getAccountRepository()
        val transactionRepository: TransactionRepository = repositoryManager.getLedgerRepository()
        val manager = AccountManager(accountRepository, AccountValidator(IDValidator()))

        val incomingAccount = Account("Incoming")
        val outgoingAccount = Account("Sending")
        accountRepository.create(incomingAccount)
        accountRepository.create(outgoingAccount)
        transactionRepository.create(randomTransfer(incomingAccount.id, outgoingAccount.id))

        assertThat {
            manager.delete(outgoingAccount.id)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            containsError(ValidationError.AccountReferences(outgoingAccount.id))
        }
    }
}