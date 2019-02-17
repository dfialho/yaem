package dfialho.yaem.app

import assertk.Assert
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.toBaseError

fun Assert<String?>.errorListContainsAll(vararg errors: ValidationError) {
    val normalizedErrors = errors.map { it.toBaseError() }.toTypedArray()
    jsonListContainsAll(ValidationError.serializer(), *normalizedErrors)
}
