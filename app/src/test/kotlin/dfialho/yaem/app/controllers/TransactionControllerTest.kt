package dfialho.yaem.app.controllers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.repositories.ParentMissingException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.validators.IDValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import io.mockk.*
import org.junit.Test
import java.sql.SQLException

class TransactionControllerTest {

    @Test
    fun `when the transaction is invalid no transaction is created`() {
        val validator = mockk<TransactionValidator>()
        val repository = mockk<TransactionRepository>()
        val controller = TransactionController(repository, validator)
        val transaction = Transaction(receiver = randomID(), amount = 10.5)

        every { validator.validate(transaction) } returns listOf(ValidationError.InvalidID(transaction.id))
        every { repository.create(any()) } just Runs

        assertThat {
            controller.create(transaction)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `creating a transaction for existing account should invoke the repository`() {
        val validator = spyk(TransactionValidator(IDValidator()))
        val repository = mockk<TransactionRepository>()
        val controller = TransactionController(repository, validator)
        val transaction = Transaction(receiver = randomID(), amount = 10.5)

        every { repository.create(any()) } just Runs

        controller.create(transaction)

        verify { repository.create(transaction) }
    }

    @Test
    fun `creating a transaction for non-existing account should throw validation error`() {
        val validator = spyk(TransactionValidator(IDValidator()))
        val repository = mockk<TransactionRepository>()
        val controller = TransactionController(repository, validator)
        val nonExistingAccount = randomID()
        val transaction = Transaction(receiver = nonExistingAccount, amount = 10.5)

        every { repository.create(any()) } throws ParentMissingException(SQLException())

        assertThat {
            controller.create(transaction)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
            containsError(ValidationError.LedgerMissingAccount())
        }
    }

    // TODO validate no concurrency issues occur between creating a transaction for an account and deleting the account
}