package dfialho.yaem.app.repositories

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction
import dfialho.yaem.app.exceptions.FoundException
import dfialho.yaem.app.exceptions.ParentMissingException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedLedgerRepository(private val accountRepository: AccountRepository) : LedgerRepository, ExposedRepository {

    private object Transactions : Table() {
        val id = uuid("ID").primaryKey()
        val timestamp = datetime("TIMESTAMP")
        val amount = double("AMOUNT")
        val description = text("DESCRIPTION")
        val incomingAccount = uuid("INCOMING_ACCOUNT") references ExposedAccountRepository.accountIDColumn
        val sendingAccount = (uuid("SENDING_ACCOUNT") references ExposedAccountRepository.accountIDColumn).nullable()
    }

    override fun createTablesIfMissing() {
        transaction {
            SchemaUtils.create(Transactions)
        }
    }

    override fun create(transaction: Transaction): Transaction = transaction {

        Transactions.insertUnique {
            it[id] = transaction.id.toUUID()
            it[timestamp] = transaction.timestamp.toDateTime()
            it[amount] = transaction.amount
            it[description] = transaction.description
            it[incomingAccount] = transaction.incomingAccount.toUUID()
            it[sendingAccount] = transaction.sendingAccount?.toUUID()
        }.applyOnDuplicateKey {
            throw FoundException("Duplicate key: transaction with id '${transaction.id}' already exists")
        }.applyOnParentMissing {

            if (!accountRepository.exists(transaction.incomingAccount)) {
                throw ParentMissingException(transaction.incomingAccount)
            } else if (transaction.sendingAccount != null && !accountRepository.exists(transaction.sendingAccount)) {
                throw ParentMissingException(transaction.sendingAccount)
            } else {
                throw ParentMissingException()
            }

        }.onFailureThrowException()

        return@transaction transaction
    }

    override fun get(transactionID: ID): Transaction? = transaction {
        val transactionUUID = transactionID.toUUID()

        return@transaction Transactions.select { Transactions.id eq transactionUUID }
            .limit(1)
            .mapToTransaction()
            .firstOrNull()
    }

    override fun list(): List<Transaction> = transaction {
        return@transaction Transactions.selectAll().mapToTransaction()
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