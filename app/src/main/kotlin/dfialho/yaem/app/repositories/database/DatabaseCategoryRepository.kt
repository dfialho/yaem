package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.api.Category
import dfialho.yaem.app.repositories.CategoryRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.utils.*
import org.jetbrains.exposed.sql.*

class DatabaseCategoryRepository(private val translator: SQLExceptionTranslator) : CategoryRepository,
    DatabaseRepository {

    override fun createTablesIfMissing() {
        transaction(translator) {
            SchemaUtils.create(Categories, SubCategories)
        }
    }

    override fun create(category: Category) {
        transaction(translator) {
            Categories.insert {
                it[name] = category.name
            }
        }
    }

    override fun create(category: String, subCategory: String) {
        transaction(translator) {
            SubCategories.insert {
                it[this.category] = category
                it[name] = subCategory
            }
        }
    }

    override fun get(category: String): Category {
        return transaction(translator) {

            SubCategories
                .select { SubCategories.category eq category }
                .groupBy({ it[SubCategories.category] }, { it[SubCategories.name] })
                .map { Category(it.key, it.value) }
                .firstOrNull()
                ?: (Categories.select { Categories.name eq category }
                    .limit(1)
                    .mapToCategory()
                    .firstOrNull()
                    ?: throwNotFound(category))
        }
    }

    override fun list(): List<Category> {
        return transaction(translator) {
            Categories.selectAll().mapToCategory()
        }
    }

    override fun rename(oldName: String, newName: String) {
        transaction(translator) {
            val updatedCount = Categories.update({ Categories.name eq oldName }) {
                it[name] = newName
            }

            if (updatedCount == 0) {
                throwNotFound(oldName)
            }
        }
    }

    override fun delete(category: String) {
        transaction(translator) {
            val deleteCount = Categories.deleteWhere { Categories.name eq category }

            if (deleteCount == 0) {
                throwNotFound(category)
            }
        }
    }

    override fun delete(category: String, subCategory: String) {
        transaction(translator) {
            val deleteCount = SubCategories.deleteWhere {
                (SubCategories.category eq category) and (SubCategories.name eq subCategory)
            }

            if (deleteCount == 0) {
                throwNotFound(category)
            }
        }
    }

    private fun Query.mapToCategory(): List<Category> {
        return this.map {
            Category(it[Categories.name])
        }
    }

    private fun throwNotFound(category: String): Nothing {
        throw NotFoundException("No category found named '$category'")
    }
}