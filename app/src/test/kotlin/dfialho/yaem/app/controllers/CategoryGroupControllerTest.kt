package dfialho.yaem.app.controllers

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.CategoryGroup
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.resources.anyCategoryGroup
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.CategoryGroupValidator
import dfialho.yaem.app.validators.ValidationError
import io.kotlintest.specs.StringSpec
import java.time.Instant

class CategoryGroupControllerTest : StringSpec({

    resourceControllerTests<CategoryGroup> {
        val manager = uniqueRepositoryManager()

        controller = CategoryGroupController(manager.getCategoryGroupRepository(), CategoryGroupValidator())
        anyResource = { anyCategoryGroup() }
        invalidResource = { CategoryGroup("   ") }
        copy = { id -> copy(id = id) }
        update = { copy(name = "$name-updated") }
        notFoundError = ValidationError.Categories::NotFound
    }

    "creating a CategoryGroup with an existing name throws an error" {
        val manager = uniqueRepositoryManager()
        val controller = CategoryGroupController(manager.getCategoryGroupRepository(), CategoryGroupValidator())
        val existingCategoryGroups = (1..3)
            .map { CategoryGroup("Group-$it") }
            .map { controller.create(it) }
            .toTypedArray()
        val existingCategoryGroup = existingCategoryGroups[1]

        assertThat {
            controller.create(CategoryGroup(existingCategoryGroup.name, id = randomID()))
        }.thrownValidationError {
            ValidationError.Categories.NameExists(existingCategoryGroup.name)
        }

        assertThat(controller.list())
            .containsOnly(*existingCategoryGroups)
    }

    "updating a CategoryGroup's name to an existing name should throw exception" {
        val manager = uniqueRepositoryManager()
        val controller = CategoryGroupController(manager.getCategoryGroupRepository(), CategoryGroupValidator())
        val accounts = (1..5)
            .map { CategoryGroup("Group-$it") }
            .map { controller.create(it) }
        val originalCategoryGroup = accounts.last()
        val updatedCategoryGroup = originalCategoryGroup.copy(
            name = accounts.first().name
        )

        assertThat {
            controller.update(updatedCategoryGroup)
        }.thrownValidationError {
            ValidationError.Categories.NameExists(updatedCategoryGroup.name)
        }

        assertThat(controller.get(updatedCategoryGroup.id))
            .isEqualTo(originalCategoryGroup)
    }
})
