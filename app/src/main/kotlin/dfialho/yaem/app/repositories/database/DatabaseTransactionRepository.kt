package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.repositories.NotFoundException
import dfialho.yaem.app.repositories.TransactionRepository
import dfialho.yaem.app.repositories.utils.*
import org.jetbrains.exposed.sql.*

class DatabaseTransactionRepository(private val translator: SQLExceptionTranslator)
    : TransactionRepository, DatabaseRepository {

    override fun createTablesIfMissing() {
        transaction(translator) {
            SchemaUtils.create(Transactions)
        }
    }

    override fun create(transaction: Transaction): Unit =
        transaction(translator) {

            Transactions.insert {
                it[id] = transaction.id.toUUID()
                it[timestamp] = transaction.timestamp.toDateTime()
                it[amount] = transaction.amount
                it[description] = transaction.description
                it[receiverAccount] = transaction.receiver.toUUID()
                it[senderAccount] = transaction.sender?.toUUID()
            }
        }

    override fun get(transactionID: ID): Transaction {
        return findTransaction(transactionID) ?: throw NotFoundException("No transaction found with ID $transactionID")
    }

    override fun list(): List<Transaction> =
        transaction(translator) {
            return@transaction Transactions.selectAll().mapToTransaction()
        }

    override fun exists(transactionID: ID): Boolean =
        transaction(translator) {
            return@transaction findTransaction(transactionID) != null
        }

    override fun update(trx: Transaction): Unit =
        transaction(translator) {

            val updatedCount = Transactions.update({ Transactions.id eq trx.id.toUUID() }) {
                it[timestamp] = trx.timestamp.toDateTime()
                it[amount] = trx.amount
                it[description] = trx.description
                it[receiverAccount] = trx.receiver.toUUID()
                it[senderAccount] = trx.sender?.toUUID()
            }

            if (updatedCount == 0) {
                throw NotFoundException("Transaction with ID '${trx.id}' was not found")
            }
        }

    override fun delete(transactionID: String): Unit =
        transaction(translator) {
            val transactionUUID = transactionID.toUUID()
            val deleteCount = Transactions.deleteWhere { Transactions.id eq transactionUUID }

            if (deleteCount == 0) {
                throw NotFoundException("Transaction with ID '$transactionID' was not found")
            }
        }

    private fun findTransaction(transactionID: ID): Transaction? =
        transaction(translator) {
            val transactionUUID = transactionID.toUUID()

            return@transaction Transactions.select { Transactions.id eq transactionUUID }
                .limit(1)
                .mapToTransaction()
                .firstOrNull()
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