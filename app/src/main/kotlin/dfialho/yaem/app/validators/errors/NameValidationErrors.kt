package dfialho.yaem.app.validators.errors

@Suppress("FunctionName")
interface NameValidationErrors {
    fun TooLong(name: String): ValidationError.InvalidName.TooLong
    fun Blank(name: String): ValidationError.InvalidName.Blank
}
