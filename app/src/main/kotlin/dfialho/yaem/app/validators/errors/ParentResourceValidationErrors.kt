package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
interface ParentResourceValidationErrors {
    fun References(id: ID): ValidationError.References
}