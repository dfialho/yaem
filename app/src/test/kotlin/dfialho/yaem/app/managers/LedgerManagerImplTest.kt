package dfialho.yaem.app.managers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.randomID
import dfialho.yaem.app.repositories.LedgerRepository
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.validators.IDValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import io.mockk.*
import org.junit.Test
import java.sql.SQLException

class LedgerManagerImplTest {

    @Test
    fun `when the transaction is invalid no transaction is created`() {
        val validator = mockk<TransactionValidator>()
        val repository = mockk<LedgerRepository>()
        val manager: LedgerManager = LedgerManagerImpl(repository, validator)
        val transaction = Transaction(amount = 10.5, description = "bananas", incomingAccount = randomID())

        every { validator.validate(transaction) } returns listOf(ValidationError.InvalidID(transaction.id))
        every { repository.create(any()) } just Runs

        assertThat {
            manager.create(transaction)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `creating a transaction for existing account should invoke the repository`() {
        val validator = spyk(TransactionValidator(IDValidator()))
        val repository = mockk<LedgerRepository>()
        val manager: LedgerManager = LedgerManagerImpl(repository, validator)
        val transaction = Transaction(amount = 10.5, description = "bananas", incomingAccount = randomID())

        every { repository.create(any()) } just Runs

        manager.create(transaction)

        verify { repository.create(transaction) }
    }

    @Test
    fun `creating a transaction for non-existing account should throw validation error`() {
        val validator = spyk(TransactionValidator(IDValidator()))
        val repository = mockk<LedgerRepository>()
        val manager: LedgerManager = LedgerManagerImpl(repository, validator)
        val nonExistingAccount = randomID()
        val transaction = Transaction(amount = 10.5, description = "bananas", incomingAccount = nonExistingAccount)

        every { repository.create(any()) } throws ParentMissingException(SQLException())

        assertThat {
            manager.create(transaction)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            containsError(ValidationError.LedgerMissingAccount())
        }
    }

    // TODO validate no concurrency issues occur between creating a transaction for an account and deleting the account
}