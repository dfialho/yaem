package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID
import org.jetbrains.exposed.sql.*

class ExposedAccountRepository(private val exceptionTranslator: SQLExceptionTranslator)
    : AccountRepository, ExposedRepository {

    internal object Accounts : Table() {
        val id = uuid("ID").primaryKey()
        val name = varchar("NAME", length = ACCOUNT_NAME_MAX_LENGTH).uniqueIndex("NAME_INDEX")
        val initialBalance = double("INITIAL_BALANCE")
        val startTimestamp = datetime("START_TIMESTAMP")
    }

    override fun createTablesIfMissing() {
        repositoryTransaction(exceptionTranslator) {
            SchemaUtils.create(Accounts)
        }
    }

    override fun create(account: Account): Unit = repositoryTransaction(exceptionTranslator) {
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

    override fun list(): List<Account> = repositoryTransaction(exceptionTranslator) {
        return@repositoryTransaction Accounts.selectAll().mapToAccount()
    }

    override fun exists(accountID: ID): Boolean = repositoryTransaction(exceptionTranslator) {
        return@repositoryTransaction findAccount(accountID) != null
    }

    override fun update(account: Account): Unit = repositoryTransaction(exceptionTranslator)  {

        val updatedCount = Accounts.update({ Accounts.id eq account.id.toUUID()}) {
            it[name] = account.name
            it[initialBalance] = account.initialBalance
            it[startTimestamp] = account.startTimestamp.toDateTime()
        }

        if (updatedCount == 0) {
            throw NotFoundException("Account with ID '${account.id}' was not found")
        }
    }

    override fun delete(accountID: String): Unit = repositoryTransaction(exceptionTranslator) {
        val accountUUID = accountID.toUUID()
        val deleteCount = Accounts.deleteWhere { Accounts.id eq accountUUID }

        if (deleteCount == 0) {
            throw NotFoundException("Account with ID '$accountID' was not found")
        }
    }

    private fun findAccount(accountID: ID): Account? = repositoryTransaction(exceptionTranslator) {
        val accountUUID = accountID.toUUID()

        return@repositoryTransaction Accounts.select { Accounts.id eq accountUUID }
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
