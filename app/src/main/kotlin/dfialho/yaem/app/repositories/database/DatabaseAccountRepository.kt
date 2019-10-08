package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.utils.*
import org.jetbrains.exposed.sql.*

class DatabaseAccountRepository(private val translator: SQLExceptionTranslator)
    : AccountRepository, DatabaseRepository {

    override fun createTablesIfMissing() {
        transaction(translator) {
            SchemaUtils.create(Accounts)
        }
    }

    override fun create(resource: Account): Unit =
        transaction(translator) {
            Accounts.insert {
                it[id] = resource.id.toUUID()
                it[name] = resource.name
                it[initialBalance] = resource.initialBalance
                it[startTimestamp] = resource.startTimestamp.toDateTime()
            }
        }

    override fun get(resourceID: ID): Account {
        return findAccount(resourceID) ?: throw NotFoundException("No account found with ID $resourceID")
    }

    override fun list(): List<Account> =
        transaction(translator) {
            return@transaction Accounts.selectAll().mapToAccount()
        }

    override fun update(resource: Account): Unit =
        transaction(translator) {

            val updatedCount = Accounts.update({ Accounts.id eq resource.id.toUUID() }) {
                it[name] = resource.name
                it[initialBalance] = resource.initialBalance
                it[startTimestamp] = resource.startTimestamp.toDateTime()
            }

            if (updatedCount == 0) {
                throw NotFoundException("Account with ID '${resource.id}' was not found")
            }
        }

    override fun delete(resourceID: String): Unit =
        transaction(translator) {
            val accountUUID = resourceID.toUUID()
            val deleteCount = Accounts.deleteWhere { Accounts.id eq accountUUID }

            if (deleteCount == 0) {
                throw NotFoundException("Account with ID '$resourceID' was not found")
            }
        }

    private fun findAccount(accountID: ID): Account? =
        transaction(translator) {
            val accountUUID = accountID.toUUID()

            return@transaction Accounts.select { Accounts.id eq accountUUID }
                .limit(1)
                .mapToAccount()
                .firstOrNull()
        }

    private fun Query.mapToAccount(): List<Account> {
        return this.map {
            Account(
                id = it[Accounts.id].toID(),
                name = it[Accounts.name],
                initialBalance = it[Accounts.initialBalance],
                startTimestamp = it[Accounts.startTimestamp].toJavaInstant()
            )
        }
    }
}
