package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.randomID
import io.kotlintest.specs.AnnotationSpec
import java.time.Instant

class AccountValidatorTest : AnnotationSpec() {

    @Test
    fun `when the account is valid it should return no errors`() {
        val validator = AccountValidator()
        val account = Account(
            name = "My Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = randomID()
        )

        val validationErrors = validator.validate(account)

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the account has an invalid id it should return an invalid ID error`() {
        val validator = AccountValidator()
        val invalidID = "invalid id"
        val account = Account(
            name = "My Account",
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = invalidID
        )

        val validationErrors = validator.validate(account)

        assertThat(validationErrors).containsOnly(ValidationError.InvalidID(invalidID))
    }

    @Test
    fun `when the account name has max length size it should validate with no errors`() {
        val validator = AccountValidator()
        val account = Account(name = "A".repeat(ACCOUNT_NAME_MAX_LENGTH))

        val validationErrors = validator.validate(account)

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the account name is over the max length it should return error`() {
        val validator = AccountValidator()
        val account = Account(name = "A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1))

        val validationErrors = validator.validate(account)

        assertThat(validationErrors)
            .containsOnly(ValidationError.Accounts.Name.TooLong(account.name))
    }

    @Test
    fun `when the account's name and id are invalid it should return both errors`() {
        val validator = AccountValidator()
        val invalidID = "invalid id"
        val tooLongName = "A".repeat(ACCOUNT_NAME_MAX_LENGTH + 1)
        val account = Account(
            name = tooLongName,
            initialBalance = 10.0,
            startTimestamp = Instant.ofEpochMilli(1550250740735),
            id = invalidID
        )

        val validationErrors = validator.validate(account)

        assertThat(validationErrors).containsAll(
            ValidationError.Accounts.Name.TooLong(tooLongName),
            ValidationError.InvalidID(invalidID)
        )
    }
}