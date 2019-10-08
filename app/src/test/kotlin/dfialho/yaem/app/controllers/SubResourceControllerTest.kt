package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.Resource
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.validators.errors.ParentResourceValidationErrors
import dfialho.yaem.app.validators.errors.SubResourceValidationErrors
import io.kotlintest.specs.AbstractStringSpec

class SubResourceControllerTestSetup<R : Resource, P : Resource> {
    lateinit var parentController: ResourceController<P>
    lateinit var controller: ResourceController<R>
    lateinit var anyParent: () -> P
    lateinit var anyResource: (parentId: String) -> R
    lateinit var update: R.(parentId: String) -> R
    lateinit var parentValidationErrors: ParentResourceValidationErrors
    lateinit var validationErrors: SubResourceValidationErrors
}

inline fun <reified R : Resource, reified P : Resource> AbstractStringSpec.subResourceControllerTests(
    parentName: String,
    crossinline setup: SubResourceControllerTestSetup<R, P>.() -> Unit
) {
    val resourceName = R::class.simpleName
    val test = SubResourceControllerTestSetup<R, P>()

    "creating a $resourceName for non-existing $parentName should throw error" {
        setup(test)
        val controller = test.controller
        val parentController = test.parentController
        parentController.create(test.anyParent())
        val nonExistingParentID = randomID()

        assertThat {
            controller.create(test.anyResource(nonExistingParentID))
        }.thrownValidationError {
            test.validationErrors.MissingDependency()
        }

        assertThat(controller.list())
            .isEmpty()
    }

    "updating a $resourceName to a non-existing $parentName should throw error" {
        setup(test)
        val update = test.update
        val controller = test.controller
        val parentController = test.parentController
        val parent = parentController.create(test.anyParent())
        val resources = (1..5)
            .map { test.anyResource(parent.id) }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingParentID = randomID()
        val originalResource = resources[1]
        val resourceToUpdate = originalResource.update(nonExistingParentID)

        assertThat {
            controller.update(resourceToUpdate)
        }.thrownValidationError {
            test.validationErrors.MissingDependency()
        }

        assertAll {
            assertThat(controller.get(resourceToUpdate.id))
                .isEqualTo(originalResource)

            assertThat(controller.list())
                .contains(originalResource)

            assertThat(controller.list())
                .doesNotContain(resourceToUpdate)
        }
    }

    "deleting $parentName referenced by at least one $resourceName should throw error" {
        setup(test)
        val controller = test.controller
        val parentController = test.parentController
        val parent = parentController.create(test.anyParent())
        val resources = (1..5)
            .map { test.anyResource(parent.id) }
            .map { controller.create(it) }
            .toTypedArray()

        assertThat {
            parentController.delete(parent.id)
        }.thrownValidationError {
            test.parentValidationErrors.References(parent.id)
        }

        assertAll {
            assertThat(parentController.get(parent.id))
                .isEqualTo(parent)

            assertThat(controller.list())
                .containsAll(*resources)
        }
    }

    "deleting $parentName after all transactions referring have been deleted should succeed" {
        setup(test)
        val controller = test.controller
        val parentController = test.parentController
        val parent1 = parentController.create(test.anyParent())
        val parent2 = parentController.create(test.anyParent())
        val resourcesFromParent1 = (1..5)
            .map { test.anyResource(parent1.id) }
            .map { controller.create(it) }
            .toTypedArray()
        val resourcesFromParent2 = (1..5)
            .map { test.anyResource(parent2.id) }
            .map { controller.create(it) }
            .toTypedArray()

        resourcesFromParent1
            .forEach { controller.delete(it.id) }

        parentController.delete(parent1.id)

        assertAll {
            assertThat(parentController.list())
                .doesNotContain(parent1)
            assertThat(controller.list())
                .containsOnly(*resourcesFromParent2)
        }
    }
}
