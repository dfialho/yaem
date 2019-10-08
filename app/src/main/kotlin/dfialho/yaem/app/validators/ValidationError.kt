package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import kotlinx.serialization.Serializable

@Suppress("FunctionName")
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

    class NotFound(code: String, resourceName: String, resourceID: ID) : ValidationError(
        code,
        message = "$resourceName with identifier '$resourceID' was not found"
    )

    class MissingDependency(code: String, dependencyName: String, dependencyID: ID? = null) : ValidationError(
        code,
        message = "Missing required dependency: $dependencyName" +
                if (dependencyID != null) " with identifier '$dependencyID'" else ""
    )

    class References(code: String, resourceName: String, resourceID: ID) : ValidationError(
        code,
        message = "$resourceName '$resourceID' is still being referenced by another resource"
    )

    abstract class Exists(code: String, resourceName: String, propertyName: String, propertyValue: String) :
        ValidationError(
            code,
            message = "$resourceName with $propertyName '$propertyValue' already exists"
        )

    sealed class InvalidName(code: String, resourceName: String, name: String, explanation: String) : ValidationError(
        code,
        message = "$resourceName name '$name' is invalid: $explanation"
    ) {
        class TooLong(code: String, resourceName: String, name: String, maxLength: Int) : InvalidName(
            code, resourceName, name,
            explanation = "it is too long (max=${Account.NAME_MAX_LENGTH}): $name (size=${name.length})"
        )

        class Blank(code: String, resourceName: String, name: String) : InvalidName(
            code, resourceName, name,
            explanation = "it cannot be blank"
        )
    }

    interface Resource {
        fun NotFound(id: ID): NotFound
    }

    interface SubResource {
        fun MissingDependency(dependencyID: ID? = null): MissingDependency
    }

    interface ParentResource {
        fun References(id: ID): References
    }

    interface Name {
        fun TooLong(name: String): InvalidName
        fun Blank(name: String): InvalidName
    }

    object Transactions : Resource, SubResource {
        const val LABEL = "TRANSACTION"
        const val NAME = "Transaction"

        override fun NotFound(id: ID) = NotFound(
            code = "$LABEL-01",
            resourceName = NAME,
            resourceID = id
        )

        override fun MissingDependency(dependencyID: ID?) = MissingDependency(
            code = "$LABEL-02",
            dependencyName = Accounts.NAME,
            dependencyID = dependencyID
        )

        class CommonAccounts(commonID: ID) : ValidationError(
            code = "$LABEL-03",
            message = "$NAME's receiver and sender accounts cannot have the same id: $commonID"
        )
    }

    object Accounts : Resource, ParentResource {
        const val LABEL = "ACCOUNT"
        const val NAME = "Account"

        override fun NotFound(id: ID) = NotFound(
            code = "${LABEL}-01",
            resourceName = NAME,
            resourceID = id
        )

        override fun References(id: ID) = References(
            code = "$LABEL-02",
            resourceName = NAME,
            resourceID = id
        )

        class NameExists(name: String) : ValidationError.Exists(
            code = "$LABEL-03",
            resourceName = NAME,
            propertyName = "name",
            propertyValue = name
        )

        object Name : ValidationError.Name {
            override fun TooLong(name: String) = InvalidName.TooLong(
                code = "$LABEL-NAME-01",
                resourceName = NAME,
                name = name,
                maxLength = Account.NAME_MAX_LENGTH
            )

            override fun Blank(name: String) = InvalidName.Blank(
                code = "$LABEL-NAME-02",
                resourceName = NAME,
                name = name
            )
        }
    }

    object Categories : Resource, ParentResource {
        const val LABEL = "CATEGORY"
        const val NAME = "Category"

        override fun NotFound(id: ID) = NotFound(
            code = "$LABEL-01",
            resourceName = NAME,
            resourceID = id
        )

        override fun References(id: ID) = References(
            code = "$LABEL-02",
            resourceName = NAME,
            resourceID = id
        )

        class NameExists(name: String) : ValidationError.Exists(
            code = "$LABEL-03",
            resourceName = NAME,
            propertyName = "name",
            propertyValue = name
        )

        object Name : ValidationError.Name {
            override fun TooLong(name: String) = InvalidName.TooLong(
                code = "$LABEL-NAME-01",
                resourceName = NAME,
                name = name,
                maxLength = Account.NAME_MAX_LENGTH
            )

            override fun Blank(name: String) = InvalidName.Blank(
                code = "$LABEL-NAME-02",
                resourceName = NAME,
                name = name
            )
        }
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
