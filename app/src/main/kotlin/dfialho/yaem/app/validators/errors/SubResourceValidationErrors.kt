package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
interface SubResourceValidationErrors {
    fun MissingDependency(dependencyID: ID? = null): ValidationError.MissingDependency
}
