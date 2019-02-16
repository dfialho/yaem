package dfialho.yaem.app.managers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.Account
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AccountManagerImplTest {

    @Test
    fun `when the account is invalid no account is created`() {
        val repository = mockk<AccountRepository>()
        val validator = mockk<AccountValidator>()
        val manager: AccountManager = AccountManagerImpl(repository, validator)
        val account = Account("Invalid Account")

        every { validator.validate(account) } returns listOf(ValidationError.InvalidID(account.id))

        assertThat {
            manager.create(account)
        }.thrownError {
            isInstanceOf(ValidationErrorException::class)
        }

        verify { repository.create(any()) wasNot Called }
    }
}