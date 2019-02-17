package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.randomID
import org.junit.Test
import java.time.Instant

class TransactionValidatorTest {

    @Test
    fun `when the transaction has an invalid id it should return an invalid ID error`() {
        val validator = TransactionValidator(IDValidator())
        val invalidID = "invalid id"
        val transaction = Transaction(
            id = invalidID,
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = randomID(),
            amount = 10.5,
            description = "bananas"
        )
        val validationErrors = validator.validate(transaction)

        assertThat(validationErrors).containsOnly(ValidationError.InvalidID(invalidID))
    }


    @Test
    fun `when the transaction has no sending transaction it should return no errors`() {
        val validator = TransactionValidator(IDValidator())
        val transaction = Transaction(
            id = randomID(),
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = randomID(),
            amount = 10.5,
            description = "bananas"
        )

        val validationErrors = validator.validate(transaction)

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the transaction has a sending account it should return no errors`() {
        val validator = TransactionValidator(IDValidator())
        val transaction = Transaction(
            id = randomID(),
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = randomID(),
            sendingAccount = randomID(),
            amount = 10.5,
            description = "bananas"
        )

        val validationErrors = validator.validate(transaction)

        assertThat(validationErrors).isEmpty()
    }

    @Test
    fun `when the transaction has the same sending and incoming accounts it should return an error`() {
        val validator = TransactionValidator(IDValidator())
        val commonAccountID = randomID()
        val transaction = Transaction(
            id = randomID(),
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = commonAccountID,
            sendingAccount = commonAccountID,
            amount = 10.5,
            description = "bananas"
        )

        val validationErrors = validator.validate(transaction)

        assertThat(validationErrors).containsOnly(ValidationError.LedgerCommonAccounts(commonAccountID))
    }

    @Test
    fun `when the transaction has all validation errors it should return all errors`() {
        val validator = TransactionValidator(IDValidator())
        val invalidID = "invalid id"
        val commonAccountID = randomID()
        val transaction = Transaction(
            id = invalidID,
            timestamp = Instant.ofEpochMilli(1550250740735),
            incomingAccount = commonAccountID,
            sendingAccount = commonAccountID,
            amount = 10.5,
            description = "bananas"
        )

        val validationErrors = validator.validate(transaction)

        assertThat(validationErrors).containsAll(
            ValidationError.LedgerCommonAccounts(commonAccountID),
            ValidationError.InvalidID(invalidID)
        )
    }
}