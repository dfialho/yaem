package dfialho.yaem.app.validators

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.validators.errors.ValidationError
import java.util.*

fun validateID(item: ID): List<ValidationError> {
    return try {
        UUID.fromString(item)
        emptyList()
    } catch (e: IllegalArgumentException) {
        listOf(ValidationError.InvalidID(item))
    }
}
