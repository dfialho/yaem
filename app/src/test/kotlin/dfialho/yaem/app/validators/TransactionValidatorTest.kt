package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.randomID
import org.junit.Test
import java.time.Instant

class TransactionValidatorTest {

    @Test
    fun `when the transaction has a valid ID it should return no errors`() {
        val validator = TransactionValidator()
        val validID = randomID()

        val validationErrors = validator.validate(
            Transaction(
                id = validID,
                receiver = randomID(),
                amount = 10.5,
                timestamp = Instant.ofEpochMilli(1550250740735),
                description = "bananas"
            )
        )

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the transaction has an invalid ID it should return an invalid ID error`() {
        val validator = TransactionValidator()
        val invalidID = "invalid id"

        val validationErrors = validator.validate(
            Transaction(
                id = invalidID,
                receiver = randomID(),
                amount = 10.5,
                timestamp = Instant.ofEpochMilli(1550250740735),
                description = "bananas"
            )
        )

        assertThat(validationErrors).containsOnly(ValidationError.InvalidID(invalidID))
    }

    @Test
    fun `when the transfer has the same sending and receiving accounts it should return an error`() {
        val validator = TransactionValidator()
        val commonAccountID = randomID()

        val validationErrors = validator.validate(
            Transaction(
                sender = commonAccountID,
                receiver= commonAccountID,
                id = randomID(),
                timestamp = Instant.ofEpochMilli(1550250740735),
                amount = 10.5,
                description = "bananas"
            )
        )

        assertThat(validationErrors).containsOnly(ValidationError.LedgerCommonAccounts(commonAccountID))
    }

    @Test
    fun `when the transaction has multiple validation errors it should return all errors`() {
        val validator = TransactionValidator()
        val invalidID = "invalid id"
        val commonAccountID = randomID()

        val validationErrors = validator.validate(
            Transaction(
                id = invalidID,
                timestamp = Instant.ofEpochMilli(1550250740735),
                receiver= commonAccountID,
                sender = commonAccountID,
                amount = 10.5,
                description = "bananas"
            )
        )

        assertThat(validationErrors).containsAll(
            ValidationError.LedgerCommonAccounts(commonAccountID),
            ValidationError.InvalidID(invalidID)
        )
    }
}