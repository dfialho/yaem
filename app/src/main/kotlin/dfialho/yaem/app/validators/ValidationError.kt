package dfialho.yaem.app.validators

import dfialho.yaem.app.api.ID
import kotlinx.serialization.Serializable

@Serializable
open class ValidationError internal constructor(val code: String, val message: String) {

    class InvalidID(id: ID) : ValidationError(
        code = "BASE-01",
        message = "Invalid ID string: $id"
    )

    class InvalidJson(itemName: String) : ValidationError(
        code = "BASE-02",
        message = "Failed to parse '$itemName' from json"
    )

    class NameTooLong(name: String, max: Int) : ValidationError(
        code = "BASE-03",
        message = "Name is too long (max=$max): $name (${name.length})"
    )

    class NotFound(resourceName: String, resourceID: ID) : ValidationError(
        code = "BASE-04",
        message = "Resource '$resourceName' with ID '$resourceID' was not found"
    )

    class AccountReferences(accountID: ID) : ValidationError(
        code = "ACCOUNT-01",
        message = "Account '$accountID' is still being referenced by at least one transaction"
    )

    class AccountNameExists(name: String) : ValidationError(
        code = "ACCOUNT-02",
        message = "Account with name '$name' already exists"
    )

    class LedgerMissingAccount(accountID: ID? = null) : ValidationError(
        code = "LEDGER-01",
        message = "Transaction depends on account ${(if (accountID == null) "" else "with id '$accountID' ")}which does not exist"
    )

    class LedgerCommonAccounts(accountID: ID) : ValidationError(
        code = "LEDGER-02",
        message = "Transaction's incoming and outgoing accounts cannot have the same id: $accountID"
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValidationError

        if (code != other.code) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "ValidationError(code='$code', message='$message')"
    }
}

fun ValidationError.toBaseError(): ValidationError {
    return ValidationError(this.code, this.message)
}
