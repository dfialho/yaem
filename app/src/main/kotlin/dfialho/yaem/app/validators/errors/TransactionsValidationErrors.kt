package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
object TransactionsValidationErrors : ResourceValidationErrors, SubResourceValidationErrors {
    const val LABEL = "TRANSACTION"
    const val NAME = "Transaction"

    override fun NotFound(id: ID) = ValidationErrors.NotFound(NAME, id)
    override fun MissingDependency(dependencyID: ID?) = ValidationErrors.MissingDependency(NAME, dependencyID)

    class CommonAccounts(commonID: ID) : ValidationError(
        code = "$LABEL-01",
        message = "$NAME's receiver and sender accounts cannot have the same ID: $commonID"
    )
}

