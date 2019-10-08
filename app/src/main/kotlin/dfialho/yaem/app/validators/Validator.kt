package dfialho.yaem.app.validators

import dfialho.yaem.app.validators.errors.ValidationError

interface Validator<T> {
    fun validate(item: T): List<ValidationError>
}