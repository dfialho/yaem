package dfialho.yaem.app

import assertk.Assert
import dfialho.yaem.app.validators.ValidationError

fun Assert<String?>.errorListContainsAll(vararg errors: ValidationError) {
    jsonListContainsAll(ValidationError.serializer(), *errors)
}
