package dfialho.yaem.app.repositories.exposed

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.exceptions.NotFoundException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.repositories.exposed.ExposedAccountRepository.Accounts
import dfialho.yaem.app.repositories.exceptions.SQLExceptionTranslator
import dfialho.yaem.app.repositories.utils.*
import org.jetbrains.exposed.sql.*

class ExposedTransactionRepository(private val exceptionTranslator: SQLExceptionTranslator)
    : TransactionRepository, ExposedRepository {

    internal object Transactions : Table() {
        val id = uuid("ID").primaryKey()
        val timestamp = datetime("TIMESTAMP")
        val amount = double("AMOUNT")
        val description = text("DESCRIPTION")
        val receiverAccount = uuid("RECEIVER_ACCOUNT") references Accounts.id
        val senderAccount = (uuid("SENDER_ACCOUNT") references Accounts.id).nullable()
    }

    override fun createTablesIfMissing() {
        repositoryTransaction(exceptionTranslator) {
            SchemaUtils.create(Transactions)
        }
    }

    override fun create(transaction: Transaction): Unit =
        repositoryTransaction(exceptionTranslator) {

            Transactions.insert {
                it[id] = transaction.id.toUUID()
                it[timestamp] = transaction.timestamp.toDateTime()
                it[amount] = transaction.amount
                it[description] = transaction.description
                it[receiverAccount] = transaction.receiver.toUUID()
                it[senderAccount] = transaction.sender?.toUUID()
            }
        }

    override fun get(transactionID: ID): Transaction? =
        repositoryTransaction(exceptionTranslator) {
            val transactionUUID = transactionID.toUUID()

            return@repositoryTransaction Transactions.select { Transactions.id eq transactionUUID }
                .limit(1)
                .mapToTransaction()
                .firstOrNull()
        }

    override fun list(): List<Transaction> =
        repositoryTransaction(exceptionTranslator) {
            return@repositoryTransaction Transactions.selectAll().mapToTransaction()
        }

    override fun exists(transactionID: ID): Boolean =
        repositoryTransaction(exceptionTranslator) {
            return@repositoryTransaction get(transactionID) != null
        }

    override fun update(trxID: String, trx: Transaction): Unit =
        repositoryTransaction(exceptionTranslator) {

            val updatedCount = Transactions.update({ Transactions.id eq trxID.toUUID() }) {
                it[timestamp] = trx.timestamp.toDateTime()
                it[amount] = trx.amount
                it[description] = trx.description
                it[receiverAccount] = trx.receiver.toUUID()
                it[senderAccount] = trx.sender?.toUUID()
            }

            if (updatedCount == 0) {
                throw NotFoundException("Transaction with ID '$trxID' was not found")
            }
        }

    override fun delete(transactionID: String): Unit =
        repositoryTransaction(exceptionTranslator) {
            val transactionUUID = transactionID.toUUID()
            val deleteCount = Transactions.deleteWhere { Transactions.id eq transactionUUID }

            if (deleteCount == 0) {
                throw NotFoundException("Transaction with ID '$transactionID' was not found")
            }
        }

    private fun Query.mapToTransaction(): List<Transaction> {
        return this.map {
            Transaction(
                amount = it[Transactions.amount],
                receiver = it[Transactions.receiverAccount].toID(),
                sender = it[Transactions.senderAccount]?.toID(),
                description = it[Transactions.description],
                timestamp = it[Transactions.timestamp].toJavaInstant(),
                id = it[Transactions.id].toID()
            )
        }
    }
}