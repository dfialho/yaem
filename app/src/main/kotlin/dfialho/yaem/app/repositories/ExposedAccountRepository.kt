package dfialho.yaem.app.repositories

import dfialho.yaem.app.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.Account
import dfialho.yaem.app.ID
import dfialho.yaem.app.exceptions.FoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedAccountRepository : AccountRepository, ExposedRepository {

    private object Accounts : Table() {
        val id = uuid("ID").primaryKey()
        val name = varchar("NAME", length = ACCOUNT_NAME_MAX_LENGTH)
        val initialBalance = double("INITIAL_BALANCE")
        val startTimestamp = datetime("START_TIMESTAMP")
    }

    companion object {
        val accountIDColumn get() = Accounts.id
    }

    override fun createTablesIfMissing() {
        transaction {
            SchemaUtils.create(Accounts)
        }
    }

    override fun create(account: Account): Account = transaction {
        Accounts.insertUnique {
            it[id] = account.id.toUUID()
            it[name] = account.name
            it[initialBalance] = account.initialBalance
            it[startTimestamp] = account.startTimestamp.toDateTime()
        }.applyOnDuplicateKey {
            throw FoundException("Duplicate key: account with id '${account.id}' already exists")
        }.onFailureThrowException()

        return@transaction account
    }

    override fun get(accountID: ID): Account? = transaction {
        val accountUUID = accountID.toUUID()

        return@transaction Accounts.select { Accounts.id eq accountUUID }
            .limit(1)
            .mapToAccount()
            .firstOrNull()
    }

    override fun list(): List<Account> = transaction {
        Accounts.selectAll().mapToAccount()
    }

    override fun exists(accountID: ID): Boolean = transaction {
        return@transaction get(accountID) != null
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
