package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.Category

/**
 * Repository holding Transactions.
 */
interface CategoryRepository {

    fun create(category: Category)

    fun create(category: String, subCategory: String)

    fun get(category: String): Category

    fun list(): List<Category>

    fun rename(oldName: String, newName: String)

    fun delete(category: String)

    fun delete(category: String, subCategory: String)
}