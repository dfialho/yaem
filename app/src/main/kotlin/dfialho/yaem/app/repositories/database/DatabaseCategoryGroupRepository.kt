package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.api.CategoryGroup
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.CategoryGroupRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.utils.toID
import dfialho.yaem.app.repositories.utils.toUUID
import dfialho.yaem.app.repositories.utils.transaction
import org.jetbrains.exposed.sql.*

class DatabaseCategoryGroupRepository(private val translator: SQLExceptionTranslator) : CategoryGroupRepository, DatabaseRepository {

    override fun createTablesIfMissing() {
        transaction(translator) {
            SchemaUtils.create(CategoryGroups)
        }
    }

    override fun create(resource: CategoryGroup) {
        transaction(translator) {
            CategoryGroups.insert {
                it[id] = resource.id.toUUID()
                it[name] = resource.name
            }
        }
    }

    override fun get(resourceID: ID): CategoryGroup {
        return transaction(translator) {
            val resourceUUID = resourceID.toUUID()

            CategoryGroups.select { CategoryGroups.id eq resourceUUID }
                .limit(1)
                .mapToCategoryGroup()
                .firstOrNull()
                ?: throw notFoundException(resourceID)
        }
    }

    override fun list(): List<CategoryGroup> {
        return transaction(translator) {
            CategoryGroups.selectAll().mapToCategoryGroup()
        }
    }

    override fun update(resource: CategoryGroup) {
        transaction(translator) {

            val updatedCount = CategoryGroups.update({ CategoryGroups.id eq resource.id.toUUID() }) {
                it[name] = resource.name
            }

            if (updatedCount == 0) {
                throw notFoundException(resource.id)
            }
        }
    }

    override fun delete(resourceID: String) {
        transaction(translator) {
            val categoryGroupUUID = resourceID.toUUID()
            val deleteCount = CategoryGroups.deleteWhere { CategoryGroups.id eq categoryGroupUUID }

            if (deleteCount == 0) {
                throw notFoundException(resourceID)
            }
        }
    }

    private fun Query.mapToCategoryGroup(): List<CategoryGroup> {
        return this.map {
            CategoryGroup(
                id = it[CategoryGroups.id].toID(),
                name = it[CategoryGroups.name]
            )
        }
    }

    private fun notFoundException(resourceID: String) =
        NotFoundException("Category group with ID '$resourceID' was not found")
}
