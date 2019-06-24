package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import dfialho.yaem.app.api.OneWayTransaction
import dfialho.yaem.app.api.Transfer
import dfialho.yaem.app.api.randomID
import org.junit.Test
import java.time.Instant

class TransactionValidatorTest {

    @Test
    fun `when the transaction has a valid ID it should return no errors`() {
        val validator = TransactionValidator(IDValidator())
        val validID = randomID()

        val validationErrors = validator.validate(OneWayTransaction(
            id = validID,
            account = randomID(),
            amount = 10.5,
            timestamp = Instant.ofEpochMilli(1550250740735),
            description = "bananas"
        ))

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the transaction has an invalid ID it should return an invalid ID error`() {
        val validator = TransactionValidator(IDValidator())
        val invalidID = "invalid id"

        val validationErrors = validator.validate(OneWayTransaction(
            id = invalidID,
            account = randomID(),
            amount = 10.5,
            timestamp = Instant.ofEpochMilli(1550250740735),
            description = "bananas"
        ))

        assertThat(validationErrors).containsOnly(ValidationError.InvalidID(invalidID))
    }

    @Test
    fun `when the transfer has the same sending and incoming accounts it should return an error`() {
        val validator = TransactionValidator(IDValidator())
        val commonAccountID = randomID()

        val validationErrors = validator.validate(Transfer(
            outgoingAccount = commonAccountID,
            incomingAccount = commonAccountID,
            id = randomID(),
            timestamp = Instant.ofEpochMilli(1550250740735),
            amount = 10.5,
            description = "bananas"
        ))

        assertThat(validationErrors).containsOnly(ValidationError.LedgerCommonAccounts(commonAccountID))
    }

    @Test
    fun `when the transaction has multiple validation errors it should return all errors`() {
        val validator = TransactionValidator(IDValidator())
        val invalidID = "invalid id"
        val commonAccountID = randomID()

        val validationErrors = validator.validate(Transfer(
            id = invalidID,
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = commonAccountID,
            outgoingAccount = commonAccountID,
            amount = 10.5,
            description = "bananas"
        ))

        assertThat(validationErrors).containsAll(
            ValidationError.LedgerCommonAccounts(commonAccountID),
            ValidationError.InvalidID(invalidID)
        )
    }
}