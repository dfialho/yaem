package dfialho.yaem.app.repositories

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction
import org.jetbrains.exposed.sql.*

class ExposedLedgerRepository(private val exceptionTranslator: SQLExceptionTranslator) : LedgerRepository,
    ExposedRepository {

    private object Transactions : Table() {
        val id = uuid("ID").primaryKey()
        val timestamp = datetime("TIMESTAMP")
        val amount = double("AMOUNT")
        val description = text("DESCRIPTION")
        val incomingAccount = uuid("INCOMING_ACCOUNT") references ExposedAccountRepository.accountIDColumn
        val sendingAccount = (uuid("SENDING_ACCOUNT") references ExposedAccountRepository.accountIDColumn).nullable()
    }

    override fun createTablesIfMissing() {
        repositoryTransaction(exceptionTranslator) {
            SchemaUtils.create(Transactions)
        }
    }

    override fun create(transaction: Transaction): Unit = repositoryTransaction(exceptionTranslator) {

        Transactions.insert {
            it[id] = transaction.id.toUUID()
            it[timestamp] = transaction.timestamp.toDateTime()
            it[amount] = transaction.amount
            it[description] = transaction.description
            it[incomingAccount] = transaction.incomingAccount.toUUID()
            it[sendingAccount] = transaction.sendingAccount?.toUUID()
        }
    }

    override fun get(transactionID: ID): Transaction? = repositoryTransaction(exceptionTranslator) {
        val transactionUUID = transactionID.toUUID()

        return@repositoryTransaction Transactions.select { Transactions.id eq transactionUUID }
            .limit(1)
            .mapToTransaction()
            .firstOrNull()
    }

    override fun list(): List<Transaction> = repositoryTransaction(exceptionTranslator) {
        return@repositoryTransaction Transactions.selectAll().mapToTransaction()
    }

    override fun exists(transactionID: ID): Boolean = repositoryTransaction(exceptionTranslator) {
        return@repositoryTransaction get(transactionID) != null
    }

    private fun Query.mapToTransaction(): List<Transaction> {
        return this.map {
            Transaction(
                id = it[Transactions.id].toID(),
                timestamp = it[Transactions.timestamp].toJavaInstant(),
                amount = it[Transactions.amount],
                description = it[Transactions.description],
                incomingAccount = it[Transactions.incomingAccount].toID(),
                sendingAccount = it[Transactions.sendingAccount]?.toID()
            )
        }
    }
}