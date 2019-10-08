package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
interface ResourceValidationErrors {
    fun NotFound(id: ID): ValidationError.NotFound
}