package dfialho.yaem.app.controllers

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory
import dfialho.yaem.app.testutils.thrownValidationError
import dfialho.yaem.app.testutils.uniqueRepositoryManager
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.CategoryValidator
import dfialho.yaem.app.validators.TransactionValidator
import dfialho.yaem.app.validators.ValidationError
import org.junit.Before
import org.junit.Test

class CategoriesControllerTest {

    lateinit var controller: CategoryController
    lateinit var accountController: AccountController
    lateinit var trxController: TransactionController

    @Before
    fun setUp() {
        val manager = uniqueRepositoryManager()
        controller = CategoryController(manager.getCategoryRepository(), CategoryValidator())
        accountController = AccountController(manager.getAccountRepository(), AccountValidator())
        trxController = TransactionController(manager.getTransactionRepository(), TransactionValidator())
    }

    @Test
    fun `create category`() {
        val category = Category("groceries")

        val createdCategory = controller.create(category)

        assertAll {
            assertThat(createdCategory)
                .isEqualTo(category)
            assertThat(controller.get(category.name))
                .isEqualTo(category)
            assertThat(controller.list())
                .containsOnly(category)
        }
    }

    @Test
    fun `create existing category should throw error`() {
        val category = controller.create(Category("bills"))

        assertThat {
            controller.create(category)
        }.thrownValidationError {
            ValidationError.Categories.Exists(category.name)
        }
    }

    @Test
    fun `create category with name as long as the max`() {
        val category = Category("A".repeat(Category.NAME_MAX_LENGTH))

        controller.create(category)

        assertThat(controller.list()).contains(category)
    }

    @Test
    fun `create category with invalid name should throw error`() {
        val category = Category("   ")

        assertThat {
            controller.create(category)
        }.thrownValidationError()
    }

    @Test
    fun `create sub-category`() {
        val category = controller.create(Category("bills"))
        val subCategory = SubCategory(category.name, "electricity")

        val createdSubCategory = controller.create(subCategory)

        assertAll {
            assertThat(createdSubCategory)
                .isEqualTo(subCategory)
            assertThat(controller.get(category.name).subCategories)
                .containsOnly(subCategory.name)
        }
    }

    @Test
    fun `create existing sub-category inside category should throw error`() {
        val category = controller.create(Category("bills"))
        val subCategory = controller.create(SubCategory(category.name, "electricity"))

        assertThat {
            controller.create(subCategory)
        }.thrownValidationError {
            ValidationError.Categories.Exists(subCategory)
        }
    }

    @Test
    fun `create existing sub-category in other category should succeed`() {
        val bills = controller.create(Category("bills"))
        val groceries = controller.create(Category("groceries"))
        val electricity = controller.create(SubCategory(bills.name, "electricity"))

        controller.create(SubCategory(groceries.name, electricity.name))

        assertThat(controller.get(groceries.name).subCategories)
            .containsOnly(electricity.name)
    }

    @Test
    fun `create sub-category for non-existing category should throw error`() {
        val category = "bills"

        assertThat {
            controller.create(SubCategory(category, "electricity"))
        }.thrownValidationError {
            ValidationError.Categories.NotFound(category)
        }
    }

    @Test
    fun `create invalid sub-category should throw error`() {
        val category = controller.create(Category("bills"))

        assertThat {
            controller.create(SubCategory(category.name, "  "))
        }.thrownValidationError()
    }

    @Test
    fun `get non-existing category should throw error`() {
        val category = "non-existing"

        assertThat {
            controller.get(category)
        }.thrownValidationError {
            ValidationError.Categories.NotFound(category)
        }
    }

    @Test
    fun `list categories before creating any returns empty list`() {
        assertThat(controller.list())
            .isEmpty()
    }

    @Test
    fun `delete a category`() {
        val bills = controller.create(Category("bills"))
        val groceries = controller.create(Category("groceries"))

        controller.delete(bills.name)

        assertAll {
            assertThat(controller.list())
                .containsOnly(groceries)

            assertThat {
                controller.get(bills.name)
            }.thrownValidationError {
                ValidationError.Categories.NotFound(bills.name)
            }
        }
    }

    @Test
    fun `list categories after deleting all returns empty list`() {
        (1..5)
            .map { Category("cat-$it") }
            .map { controller.create(it) }
            .forEach { controller.delete(it.name) }

        assertThat(controller.list())
            .isEmpty()
    }

    @Test
    fun `delete non-existing category should throw error`() {
        val category = "non-existing"

        assertThat {
            controller.delete(category)
        }.thrownValidationError {
            ValidationError.Categories.NotFound(category)
        }
    }

    @Test
    fun `delete category with multiple sub-categories deletes all`() {
        val bills = Category("bills", listOf("electricity", "water"))
        controller.create(bills)
        controller.create(Category("groceries", listOf("apples", "bananas")))
        bills.subCategories.forEach {
            controller.create(SubCategory(bills.name, it))
        }

        controller.delete(bills.name)

        assertThat(controller.list())
            .doesNotContain(bills)
    }

    @Test
    fun `delete sub-category`() {
        val bills = Category("bills", listOf("electricity", "water"))
        controller.create(bills)
        bills.subCategories.forEach { controller.create(SubCategory(bills.name, it)) }

        controller.delete(SubCategory(bills.name, "water"))

        assertThat(controller.get(bills.name).subCategories)
            .containsOnly("electricity")
    }

    @Test
    fun `delete non-existing sub-category should throw error`() {
        val bills = Category("bills", listOf("electricity", "water"))
        controller.create(bills)
        bills.subCategories.forEach { controller.create(SubCategory(bills.name, it)) }
        val nonExisting = SubCategory(bills.name, "non-existing")

        assertThat {
            controller.delete(nonExisting)
        }.thrownValidationError {
            ValidationError.Categories.NotFound(nonExisting)
        }
    }

    @Test
    fun `rename category`() {
        val groceries = controller.create(Category("groceries"))
        val bills = Category("bills", listOf("electricity", "water"))
        val utilities = Category("utilities", bills.subCategories)
        controller.create(bills)
        bills.subCategories.forEach { controller.create(SubCategory(bills.name, it)) }

        controller.rename(bills.name, utilities.name)

        assertAll {
            assertThat(controller.get(utilities.name))
                .isEqualTo(utilities)
            assertThat(controller.list())
                .containsOnly(groceries, Category(utilities.name))
        }
    }

    @Test
    fun `rename non-existing category should throw an error`() {
        val category = "non-existing"

        assertThat {
            controller.rename(category, "new-name")
        }.thrownValidationError {
            ValidationError.Categories.NotFound(category)
        }
    }

    @Test
    fun `rename category to invalid name`() {
        val bills = controller.create(Category("bills"))
        val newName = "   "

        assertThat {
            controller.rename(bills.name, newName)
        }.thrownValidationError {
            ValidationError.Categories.Name.Blank(newName)
        }
    }

    @Test
    fun `rename category to existing category should throw error`() {
        val bills = controller.create(Category("bills"))
        val groceries = controller.create(Category("groceries"))

        assertThat {
            controller.rename(bills.name, groceries.name)
        }.thrownValidationError {
            ValidationError.Categories.Exists(groceries.name)
        }
    }

    @Test
    fun `rename category to same name should succeed`() {

        val bills = controller.create(Category("bills"))
        controller.create(Category("groceries"))

        assertThat {
            controller.rename(bills.name, bills.name)
        }.doesNotThrowAnyException()
    }
}
