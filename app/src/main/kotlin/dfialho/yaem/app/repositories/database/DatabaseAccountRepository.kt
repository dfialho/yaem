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

    override fun create(account: Account): Unit =
        transaction(translator) {
            Accounts.insert {
                it[id] = account.id.toUUID()
                it[name] = account.name
                it[initialBalance] = account.initialBalance
                it[startTimestamp] = account.startTimestamp.toDateTime()
            }
        }

    override fun get(accountID: ID): Account {
        return findAccount(accountID) ?: throw NotFoundException("No account found with ID $accountID")
    }

    override fun list(): List<Account> =
        transaction(translator) {
            return@transaction Accounts.selectAll().mapToAccount()
        }

    override fun exists(accountID: ID): Boolean =
        transaction(translator) {
            return@transaction findAccount(accountID) != null
        }

    override fun update(account: Account): Unit =
        transaction(translator) {

            val updatedCount = Accounts.update({ Accounts.id eq account.id.toUUID() }) {
                it[name] = account.name
                it[initialBalance] = account.initialBalance
                it[startTimestamp] = account.startTimestamp.toDateTime()
            }

            if (updatedCount == 0) {
                throw NotFoundException("Account with ID '${account.id}' was not found")
            }
        }

    override fun delete(accountID: String): Unit =
        transaction(translator) {
            val accountUUID = accountID.toUUID()
            val deleteCount = Accounts.deleteWhere { Accounts.id eq accountUUID }

            if (deleteCount == 0) {
                throw NotFoundException("Account with ID '$accountID' was not found")
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
