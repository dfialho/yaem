package dfialho.yaem.app.validators

class ValidationErrorException(val errors: List<ValidationError>) : Exception("Some validation error(s) occurred: $errors") {

    constructor(error: ValidationError, vararg errors: ValidationError): this(listOf(error, *errors))
}

fun ValidationError.throwIt(): Nothing {
    throw ValidationErrorException(this)
}

fun throwError(error: () -> ValidationError): Nothing {
    throw ValidationErrorException(error())
}

fun throwIfValidationError(errors: List<ValidationError>) {
    if (errors.isNotEmpty()) {
        throw ValidationErrorException(errors)
    }
}
