package dfialho.yaem.app.validators

import dfialho.yaem.app.ID
import kotlinx.serialization.Serializable

@Serializable
open class ValidationError(val code: String, val message: String) {

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
        message = "Name is too long (max=$max): $name"
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

fun throwIfValidationError(errors: List<ValidationError>) {
    if (errors.isNotEmpty()) {
        throw ValidationErrorException(errors)
    }
}