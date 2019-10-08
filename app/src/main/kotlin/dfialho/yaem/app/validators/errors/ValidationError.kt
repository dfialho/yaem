package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID
import kotlinx.serialization.Serializable

@Serializable
@Suppress("FunctionName")
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

    class NameExists(code: String, resourceName: String, name: String) : ValidationError(
        code,
        message = "$resourceName with name '$name' already exists"
    )

    sealed class InvalidName(code: String, resourceName: String, name: String, explanation: String) : ValidationError(
        code,
        message = "$resourceName name '$name' is invalid: $explanation"
    ) {
        class TooLong(code: String, resourceName: String, name: String, maxLength: Int) : InvalidName(
            code, resourceName, name,
            explanation = "it is too long (max=$maxLength): $name (size=${name.length})"
        )

        class Blank(code: String, resourceName: String, name: String) : InvalidName(
            code, resourceName, name,
            explanation = "it cannot be blank"
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
