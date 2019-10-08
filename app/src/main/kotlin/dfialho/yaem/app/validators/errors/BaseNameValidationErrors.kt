package dfialho.yaem.app.validators.errors

@Suppress("FunctionName")
object BaseNameValidationErrors  {

    fun TooLong(resourceName: String, name: String, maxLength: Int) = ValidationError.InvalidName.TooLong(
        code = "NAME-01",
        resourceName = resourceName,
        name = name,
        maxLength = maxLength
    )

    fun Blank(resourceName: String, name: String) = ValidationError.InvalidName.Blank(
        code = "NAME-02",
        resourceName = resourceName,
        name = name
    )
}
