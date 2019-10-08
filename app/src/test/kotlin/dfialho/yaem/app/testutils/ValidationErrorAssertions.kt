package dfialho.yaem.app.testutils

import assertk.AssertBlock
import assertk.assertions.contains
import assertk.assertions.isInstanceOf
import dfialho.yaem.app.validators.ValidationErrorException
import dfialho.yaem.app.validators.errors.ValidationError

fun <T> AssertBlock<T>.thrownValidationError(expectedError: () -> ValidationError) {
    thrownError {
        isInstanceOf(ValidationErrorException::class)
        transform { (it as ValidationErrorException).errors }
            .contains(expectedError())
    }
}

fun <T> AssertBlock<T>.thrownValidationError() {
    thrownError {
        isInstanceOf(ValidationErrorException::class)
    }
}
