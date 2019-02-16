package dfialho.yaem.app.repositories

import dfialho.yaem.app.ACCOUNT_NAME_MAX_LENGTH
import dfialho.yaem.app.Account
import dfialho.yaem.app.ID
import dfialho.yaem.app.exceptions.FoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.time.Instant
import java.util.*

class ExposedAccountRepository(url: String, driver: String) : AccountRepository {

    init {
        Database.connect(url, driver)
        transaction {
            SchemaUtils.create(Accounts)
        }
    }

    object Accounts : Table() {
        val id = uuid("ID").primaryKey()
        val name = varchar("NAME", length = ACCOUNT_NAME_MAX_LENGTH)
        val initialBalance = double("INITIAL_BALANCE")
        val startTimestamp = datetime("START_TIMESTAMP")
    }

    override fun create(account: Account): Account = transaction {
        Accounts.insertUnique {
            it[id] = account.id.toUUID()
            it[name] = account.name
            it[initialBalance] = account.initialBalance
            it[startTimestamp] = account.startTimestamp.toDateTime()
        }.onDuplicateKey {
            throw FoundException("Duplicate key: account with id '${account.id}' already exists")
        }.onFailureThrowException()

        account
    }

    override fun get(accountID: ID): Account? = transaction {
        val accountUUID = accountID.toUUID()

        Accounts.select { Accounts.id eq accountUUID }
            .limit(1)
            .mapToAccount()
            .firstOrNull()
    }

    override fun list(): List<Account> = transaction {
        Accounts.selectAll().mapToAccount()
    }
}

private fun Query.mapToAccount(): List<Account> = this.map {
    Account(
        id = it[ExposedAccountRepository.Accounts.id].toID(),
        name = it[ExposedAccountRepository.Accounts.name],
        initialBalance = it[ExposedAccountRepository.Accounts.initialBalance],
        startTimestamp = it[ExposedAccountRepository.Accounts.startTimestamp].toJavaInstant()
    )
}

private fun ID.toUUID(): UUID = UUID.fromString(this)

private fun UUID.toID(): ID = this.toString()

private fun Instant?.toDateTime(): DateTime = DateTime(this?.toEpochMilli() ?: Instant.now())

private fun DateTime.toJavaInstant(): Instant = this.toDate().toInstant()
