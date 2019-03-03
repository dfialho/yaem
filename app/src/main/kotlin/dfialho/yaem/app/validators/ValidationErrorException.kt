package dfialho.yaem.app.validators

class ValidationErrorException(val errors: List<ValidationError>) : Exception("Some validation error(s) occurred: $errors") {

    constructor(error: ValidationError, vararg errors: ValidationError): this(listOf(error, *errors))
}