package dfialho.yaem.app.validators

class ValidationErrorException(val errors: List<ValidationError>, cause: Throwable? = null)
    : Exception("Some validation error(s) occurred: $errors", cause) {
}

fun throwError(error: () -> ValidationError): Nothing {
    throw ValidationErrorException(listOf(error()))
}

fun throwError(cause: Throwable, error: () -> ValidationError): Nothing {
    throw ValidationErrorException(listOf(error()), cause)
}

fun throwIfValidationError(errors: List<ValidationError>) {
    if (errors.isNotEmpty()) {
        throw ValidationErrorException(errors)
    }
}
