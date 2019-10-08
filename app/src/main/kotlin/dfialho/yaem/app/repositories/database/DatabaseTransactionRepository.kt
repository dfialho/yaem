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

    override fun create(resource: Transaction): Unit =
        transaction(translator) {

            Transactions.insert {
                it[id] = resource.id.toUUID()
                it[timestamp] = resource.timestamp.toDateTime()
                it[amount] = resource.amount
                it[description] = resource.description
                it[receiverAccount] = resource.receiver.toUUID()
                it[senderAccount] = resource.sender?.toUUID()
            }
        }

    override fun get(resourceID: ID): Transaction {
        return findTransaction(resourceID) ?: throw NotFoundException("No transaction found with ID $resourceID")
    }

    override fun list(): List<Transaction> =
        transaction(translator) {
            return@transaction Transactions.selectAll().mapToTransaction()
        }

    override fun update(resource: Transaction): Unit =
        transaction(translator) {

            val updatedCount = Transactions.update({ Transactions.id eq resource.id.toUUID() }) {
                it[timestamp] = resource.timestamp.toDateTime()
                it[amount] = resource.amount
                it[description] = resource.description
                it[receiverAccount] = resource.receiver.toUUID()
                it[senderAccount] = resource.sender?.toUUID()
            }

            if (updatedCount == 0) {
                throw NotFoundException("Transaction with ID '${resource.id}' was not found")
            }
        }

    override fun delete(resourceID: String): Unit =
        transaction(translator) {
            val transactionUUID = resourceID.toUUID()
            val deleteCount = Transactions.deleteWhere { Transactions.id eq transactionUUID }

            if (deleteCount == 0) {
                throw NotFoundException("Transaction with ID '$resourceID' was not found")
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