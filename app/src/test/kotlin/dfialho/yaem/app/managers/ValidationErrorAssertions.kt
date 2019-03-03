package dfialho.yaem.app.managers

import assertk.Assert
import assertk.assertions.contains
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException


fun <T : Throwable> Assert<T>.containsError(error: ValidationError) = given { actual ->
    actual as ValidationErrorException
    assertThat(actual.errors).contains(error)
}
