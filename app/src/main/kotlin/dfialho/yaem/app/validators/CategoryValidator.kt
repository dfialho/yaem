package dfialho.yaem.app.validators

import dfialho.yaem.app.api.Category

class CategoryValidator : Validator<Category> {

    override fun validate(item: Category): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (item.name.isBlank()) {
            errors += ValidationError.Categories.Name.Blank(item.name)
            return errors
        }

        if (item.name.first().isWhitespace()) {
            errors += ValidationError.Categories.Name.StartingWhitespace(item.name)
        }

        if (item.name.last().isWhitespace()) {
            errors += ValidationError.Categories.Name.EndingWhitespace(item.name)
        }

        return errors
    }
}