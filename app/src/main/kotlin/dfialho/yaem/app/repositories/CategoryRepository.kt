package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory

/**
 * Repository holding Transactions.
 */
interface CategoryRepository {
    fun create(category: Category)
    fun create(subCategory: SubCategory)
    fun get(category: String): Category
    fun list(): List<Category>
    fun rename(oldName: String, newName: String)
    fun delete(category: String)
    fun delete(subCategory: SubCategory)
}