package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import dfialho.yaem.app.api.Category
import org.junit.Test

class CategoryValidatorTest {

    val validator = CategoryValidator()

    @Test
    fun `category with letters only should be valid`() {
        val errors = validator.validate(Category("abcdef"))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `category with letters and numbers should be valid`() {
        val errors = validator.validate(Category("abcdef12345"))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `category with spaces in the middle should be valid`() {
        val errors = validator.validate(Category("abc def 12345"))
        assertThat(errors).isEmpty()
    }

    @Test
    fun `category starting with a whitespace should be invalid`() {
        val category = Category(" bills")
        val errors = validator.validate(category)

        assertThat(errors)
            .contains(ValidationError.Categories.InvalidName.StartingWhitespace(category.name))
    }

    @Test
    fun `category ending with a whitespace should be invalid`() {
        val category = Category("bills  ")
        val errors = validator.validate(category)

        assertThat(errors)
            .contains(ValidationError.Categories.InvalidName.EndingWhitespace(category.name))
    }

    @Test
    fun `category with empty name should be invalid`() {
        val category = Category("")
        val errors = validator.validate(category)

        assertThat(errors)
            .contains(ValidationError.Categories.InvalidName.Blank(category.name))
    }

    @Test
    fun `category with blank name should be invalid`() {
        val category = Category("  ")
        val errors = validator.validate(category)

        assertThat(errors)
            .contains(ValidationError.Categories.InvalidName.Blank(category.name))
    }
}