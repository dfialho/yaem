package dfialho.yaem.app.validators

interface Validator<T> {

    fun validate(item: T): List<ValidationError>
}