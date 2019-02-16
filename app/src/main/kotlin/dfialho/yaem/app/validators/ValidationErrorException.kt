package dfialho.yaem.app.validators

class ValidationErrorException(val errors: List<ValidationError>) : Exception("Some validation error(s) occurred") {

    constructor(vararg errors: ValidationError): this(errors.toList())
}