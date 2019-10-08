package dfialho.yaem.app.validators

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.CategoryGroup
import dfialho.yaem.app.testutils.resources.anyCategoryGroup
import dfialho.yaem.app.validators.errors.CategoriesValidationErrors
import dfialho.yaem.app.validators.errors.ValidationError
import io.kotlintest.specs.BehaviorSpec

class CategoryGroupValidatorTest : BehaviorSpec({

    Given("a valid category group") {
        val validator = CategoryGroupValidator()
        val group = anyCategoryGroup()

        When("validating it") {
            val validationErrors = validator.validate(group)

            Then("it should return no errors") {
                assertThat(validationErrors)
                    .isEmpty()
            }
        }
    }

    Given("a category group with an invalid ID") {
        val validator = CategoryGroupValidator()
        val group = CategoryGroup(name = "Group", id = "invalid id")

        When("validating it") {
            val validationErrors = validator.validate(group)

            Then("it should return an error") {
                assertThat(validationErrors)
                    .contains(ValidationError.InvalidID(group.id))
            }
        }
    }

    Given("a category group whose name has size equal to the max length") {
        val validator = CategoryGroupValidator()
        val group = CategoryGroup(name = "A".repeat(Category.NAME_MAX_LENGTH))

        When("validating it") {
            val validationErrors = validator.validate(group)

            Then("it should return no errors") {
                assertThat(validationErrors)
                    .isEmpty()
            }
        }
    }

    Given("a category group whose name is longer then the max length") {
        val validator = CategoryGroupValidator()
        val group = CategoryGroup(name = "A".repeat(Category.NAME_MAX_LENGTH + 1))

        When("validating it") {
            val validationErrors = validator.validate(group)

            Then("it should return an error") {
                assertThat(validationErrors)
                    .containsOnly(CategoriesValidationErrors.Name.TooLong(group.name))
            }
        }
    }

    Given("a category group with invalid ID and name longer then the max length") {
        val validator = CategoryGroupValidator()
        val group = CategoryGroup(
            name = "A".repeat(Category.NAME_MAX_LENGTH + 1),
            id = "invalid ID"
        )

        When("validating it") {
            val validationErrors = validator.validate(group)

            Then("it should return an error") {
                assertThat(validationErrors)
                    .containsAll(
                        CategoriesValidationErrors.Name.TooLong(group.name),
                        ValidationError.InvalidID(group.id)
                    )
            }
        }
    }
})
