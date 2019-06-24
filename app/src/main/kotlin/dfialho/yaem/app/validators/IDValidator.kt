package dfialho.yaem.app.validators

import dfialho.yaem.app.api.ID
import java.util.*

class IDValidator : Validator<ID> {

    override fun validate(item: ID): List<ValidationError> {
        return try {
            UUID.fromString(item)
            emptyList()
        } catch (e: IllegalArgumentException) {
            listOf(ValidationError.InvalidID(item))
        }
    }
}