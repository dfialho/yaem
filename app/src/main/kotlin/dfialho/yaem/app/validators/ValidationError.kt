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

    abstract class NotFound(code: String, resourceName: String, resourceID: ID) : ValidationError(
        code,
        message = "$resourceName with identifier '$resourceID' was not found"
    )

    abstract class MissingDependency(code: String, dependencyName: String, dependencyID: ID? = null) : ValidationError(
        code,
        message = "Missing required dependency: $dependencyName" +
                if (dependencyID != null) "with identifier '$dependencyID'" else ""
    )

    abstract class References(code: String, resourceName: String, resourceID: ID) : ValidationError(
        code,
        message = "$resourceName '$resourceID' is still being referenced by another resource"
    )

    abstract class Exists(code: String, resourceName: String, propertyName: String, propertyValue: String) : ValidationError(
        code,
        message = "$resourceName with $propertyName '$propertyValue' already exists"
    )

    object Transactions {
        const val LABEL = "TRANSACTION"
        const val NAME = "Transaction"

        class NotFound(id: ID) : ValidationError.NotFound(
            code = "$LABEL-01",
            resourceName = NAME,
            resourceID = id
        )

        class MissingAccount(accountID: ID? = null) : ValidationError.MissingDependency(
            code = "$LABEL-02",
            dependencyName = Accounts.NAME,
            dependencyID = accountID
        )

        class CommonAccounts(commonID: ID) : ValidationError(
            code = "$LABEL-03",
            message = "Transaction's receiver and sender accounts cannot have the same id: $commonID"
        )
    }

    object Accounts {
        const val LABEL = "ACCOUNT"
        const val NAME = "Account"

        class NotFound(id: ID) : ValidationError.NotFound(
            code = "$LABEL-01",
            resourceName = NAME,
            resourceID = id
        )

        class References(accountID: ID) : ValidationError.References(
            code = "$LABEL-02",
            resourceName = NAME,
            resourceID = accountID
        )

        class NameIsBlank : ValidationError(
            code = "$LABEL-03",
            message = "Account name cannot be blank"
        )

        class NameExists(name: String) : ValidationError.Exists(
            code = "$LABEL-04",
            resourceName = NAME,
            propertyName = "name",
            propertyValue = name
        )

        class NameTooLong(name: String, max: Int) : ValidationError(
            code = "$LABEL-05",
            message = "Account name is too long (max=$max): $name (size=${name.length})"
        )
    }

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
