package dfialho.yaem.app.controllers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
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

class AccountControllerTest {

    @Test
    fun `when the account is invalid the controller does not try to create the account`() {
        val repository = mockk<AccountRepository>()
        val validator = mockk<AccountValidator>()
        val controller = AccountController(repository, validator)
        val account = Account("Invalid Account")

        every { validator.validate(account) } returns listOf(ValidationError.InvalidID(account.id))
        every { repository.create(any()) } just Runs

        assertThat {
            controller.create(account)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `when no validation error occurs the controller tries to create the account`() {
        val repository = mockk<AccountRepository>(relaxed = true)
        val validator = AccountValidator(IDValidator())
        val controller = AccountController(repository, validator)
        val account = Account("Valid Account")

        controller.create(account)

        verifyAll { repository.create(account) }
    }

    @Test
    fun `when the account ID is invalid the controller does not try to delete any account`() {
        val repository = mockk<AccountRepository>()
        val validator = AccountValidator(IDValidator())
        val controller = AccountController(repository, validator)
        val accountID = "invalid id"

        every { repository.delete(accountID) } just Runs

        assertThat {
            controller.delete(accountID)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `when the account ID is valid the controller tries to delete the account`() {
        val repository = mockk<AccountRepository>(relaxed = true)
        val validator = AccountValidator(IDValidator())
        val controller = AccountController(repository, validator)
        val accountID = randomID()

        controller.delete(accountID)

        verifyAll { repository.delete(accountID) }
    }

    @Test
    fun `deleting an account which is incoming account of at least one transaction returns a validation error`() {
        val repositoryManager = uniqueRepositoryManager()
        val accountRepository: AccountRepository = repositoryManager.getAccountRepository()
        val transactionRepository: TransactionRepository = repositoryManager.getLedgerRepository()
        val controller = AccountController(accountRepository, AccountValidator(IDValidator()))

        val incomingAccount = Account("Incoming")
        val outgoingAccount = Account("Sending")
        accountRepository.create(incomingAccount)
        accountRepository.create(outgoingAccount)
        transactionRepository.create(randomTransfer(incomingAccount.id, outgoingAccount.id))

        assertThat {
            controller.delete(incomingAccount.id)
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
        val controller = AccountController(accountRepository, AccountValidator(IDValidator()))

        val incomingAccount = Account("Incoming")
        val outgoingAccount = Account("Sending")
        accountRepository.create(incomingAccount)
        accountRepository.create(outgoingAccount)
        transactionRepository.create(randomTransfer(incomingAccount.id, outgoingAccount.id))

        assertThat {
            controller.delete(outgoingAccount.id)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            containsError(ValidationError.AccountReferences(outgoingAccount.id))
        }
    }
}