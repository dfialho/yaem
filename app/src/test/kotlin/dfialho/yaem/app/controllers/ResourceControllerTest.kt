package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Resource
import dfialho.yaem.app.api.randomID
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.validators.ValidationError
import io.kotlintest.specs.AbstractStringSpec

class ResourceControllerTestSetup<R : Resource> {
    lateinit var controller: ResourceController<R>
    lateinit var anyResource: () -> R
    lateinit var invalidResource: () -> R
    lateinit var copy: R.(id: String) -> R
    lateinit var update: R.() -> R
    lateinit var notFoundError: (id: ID) -> ValidationError.NotFound
}

inline fun <reified R : Resource> AbstractStringSpec.resourceControllerTests(
    crossinline setup: ResourceControllerTestSetup<R>.() -> Unit
) {
    val resourceName = R::class.simpleName
    val test = ResourceControllerTestSetup<R>()

    "create a $resourceName" {
        setup(test)
        val copy = test.copy
        val controller = test.controller

        val resource = test.anyResource()
        val created = controller.create(resource)

        assertAll {
            assertThat(controller.get(created.id))
                .isEqualTo(resource.copy(created.id))

            assertThat(controller.get(created.id))
                .isEqualTo(created)

            assertThat(controller.list())
                .contains(created)
        }
    }

    "delete a $resourceName" {
        setup(test)
        val controller = test.controller
        val resource = controller.create(test.anyResource())

        controller.delete(resource.id)

        assertThat(controller.list())
            .doesNotContain(resource)
    }

    "create multiple $resourceName's" {
        setup(test)
        val controller = test.controller

        val created = (1..5)
            .map { test.anyResource() }
            .map { controller.create(it) }
            .toTypedArray()

        assertThat(controller.list())
            .containsAll(*created)
    }

    "create an invalid $resourceName should throw an error and not create it" {
        setup(test)
        val controller = test.controller

        assertThat {
            controller.create(test.invalidResource())
        }.thrownValidationError()

        assertThat(controller.list())
            .isEmpty()
    }

    "create multiple $resourceName with the same ID should succeed because ID is overridden" {
        setup(test)
        val copy = test.copy
        val controller = test.controller

        val resource1 = controller.create(test.anyResource())
        val resource2 = controller.create(test.anyResource().copy(resource1.id))

        assertThat(controller.list())
            .containsAll(resource1, resource2)
    }

    "getting a non-existing $resourceName should throw an error" {
        setup(test)
        val controller = test.controller
        (1..3)
            .map { test.anyResource() }
            .map { controller.create(it) }
            .toTypedArray()
        val nonExistingID = randomID()

        assertThat {
            controller.get(nonExistingID)
        }.thrownValidationError {
            test.notFoundError(nonExistingID)
        }
    }

    "listing all $resourceName's before creating any shoudl return an empty list" {
        setup(test)
        val controller = test.controller

        assertThat(controller.list())
            .isEmpty()
    }

    "deleting all $resourceName's should result in an empty list" {
        setup(test)
        val controller = test.controller
        val createdAccounts = (1..5)
            .map { test.anyResource() }
            .map { controller.create(it) }
            .toTypedArray()

        createdAccounts.forEach { controller.delete(it.id) }

        assertThat(controller.list())
            .isEmpty()
    }

    "deleting one $resourceName should remove it from the list" {
        setup(test)
        val controller = test.controller
        val others = (1..3)
            .map { test.anyResource() }
            .map { controller.create(it) }
            .toTypedArray()
        val deleted = controller.create(test.anyResource())

        controller.delete(deleted.id)

        assertAll {
            assertThat(controller.list())
                .containsOnly(*others)
        }
    }

    "deleting a non-existing $resourceName should throw an error" {
        setup(test)
        val controller = test.controller
        val nonExistingID = randomID()

        assertThat {
            controller.delete(nonExistingID)
        }.thrownValidationError {
            test.notFoundError(nonExistingID)
        }
    }

    "update a $resourceName" {
        setup(test)
        val update = test.update
        val controller = test.controller
        val existing = (1..5)
            .map { test.anyResource() }
            .map { controller.create(it) }
            .toTypedArray()
        val original = existing[1]
        val resourceToUpdate = original.update()

        val updated = controller.update(resourceToUpdate)

        assertAll {
            assertThat(updated)
                .isEqualTo(resourceToUpdate)

            assertThat(controller.get(resourceToUpdate.id))
                .isEqualTo(resourceToUpdate)

            assertThat(controller.list())
                .contains(resourceToUpdate)

            assertThat(controller.list())
                .doesNotContain(original)
        }
    }

    "updating a non-existing $resourceName should throw error" {
        setup(test)
        val controller = test.controller
        val nonExisting = test.anyResource()

        assertThat {
            controller.update(nonExisting)
        }.thrownValidationError {
            test.notFoundError(nonExisting.id)
        }
    }

    "updating to an invalid $resourceName should throw error" {
        setup(test)
        val copy = test.copy
        val controller = test.controller
        val resource = controller.create(test.anyResource())
        val invalidResource = test.invalidResource().copy(resource.id)

        assertThat {
            controller.update(invalidResource)
        }.thrownValidationError()
    }
}
