package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
object CategoriesValidationErrors : ResourceValidationErrors, ParentResourceValidationErrors {
    const val NAME = "Category"

    override fun NotFound(id: ID) = ValidationErrors.NotFound(NAME, id)
    override fun References(id: ID) = ValidationErrors.References(NAME, id)
    fun NameExists(name: String) = ValidationErrors.NameExists(NAME, name)

    object Name : NameValidationErrors {
        override fun TooLong(name: String) = BaseNameValidationErrors.TooLong(NAME, name, Account.NAME_MAX_LENGTH)
        override fun Blank(name: String) = BaseNameValidationErrors.Blank(NAME, name)
    }
}

