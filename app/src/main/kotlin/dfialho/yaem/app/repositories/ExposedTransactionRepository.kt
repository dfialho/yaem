package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.OneWayTransaction
import dfialho.yaem.app.api.Transaction
import dfialho.yaem.app.api.Transfer
import org.jetbrains.exposed.sql.*

class ExposedTransactionRepository(private val exceptionTranslator: SQLExceptionTranslator) : TransactionRepository, ExposedRepository {

    internal enum class TransactionType {
        ONE_WAY, TRANSFER
    }

    internal object Transactions : Table() {
        val id = uuid("ID").primaryKey()
        val timestamp = datetime("TIMESTAMP")
        val amount = double("AMOUNT")
        val type = enumeration("TYPE", TransactionType::class)
        val description = text("DESCRIPTION")
        val incomingAccount = uuid("INCOMING_ACCOUNT") references ExposedAccountRepository.Accounts.id
        val outgoingAccount = (uuid("OUTGOING_ACCOUNT") references ExposedAccountRepository.Accounts.id).nullable()
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

            when (transaction) {
                is OneWayTransaction -> {
                    it[type] = TransactionType.ONE_WAY
                    it[incomingAccount] = transaction.account.toUUID()
                    it[outgoingAccount] = null
                }
                is Transfer -> {
                    it[type] = TransactionType.TRANSFER
                    it[incomingAccount] = transaction.incomingAccount.toUUID()
                    it[outgoingAccount] = transaction.outgoingAccount.toUUID()
                }
            }
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

    override fun update(trxID: String, trx: Transaction): Unit = repositoryTransaction(exceptionTranslator) {

        val updatedCount = Transactions.update({ Transactions.id eq trxID.toUUID()}) {
            it[timestamp] = trx.timestamp.toDateTime()
            it[amount] = trx.amount
            it[description] = trx.description

            when (trx) {
                is OneWayTransaction -> {
                    it[type] = TransactionType.ONE_WAY
                    it[incomingAccount] = trx.account.toUUID()
                    it[outgoingAccount] = null
                }
                is Transfer -> {
                    it[type] = TransactionType.TRANSFER
                    it[incomingAccount] = trx.incomingAccount.toUUID()
                    it[outgoingAccount] = trx.outgoingAccount.toUUID()
                }
            }
        }

        if (updatedCount == 0) {
            throw NotFoundException("Transaction with ID '$trxID' was not found")
        }
    }

    override fun delete(transactionID: String): Unit = repositoryTransaction(exceptionTranslator) {
        val transactionUUID = transactionID.toUUID()
        val deleteCount = Transactions.deleteWhere { Transactions.id eq transactionUUID }

        if (deleteCount == 0) {
            throw NotFoundException("Transaction with ID '$transactionID' was not found")
        }
    }

    private fun Query.mapToTransaction(): List<Transaction> {
        return this.map {

            when (it[Transactions.type]) {
                TransactionType.ONE_WAY -> OneWayTransaction(
                    account = it[Transactions.incomingAccount].toID(),
                    amount = it[Transactions.amount],
                    description = it[Transactions.description],
                    timestamp = it[Transactions.timestamp].toJavaInstant(),
                    id = it[Transactions.id].toID()
                )
                TransactionType.TRANSFER -> Transfer(
                    outgoingAccount = it[Transactions.outgoingAccount]?.toID() ?: throw IllegalStateException("Outgoing account cannot be null"),
                    incomingAccount = it[Transactions.incomingAccount].toID(),
                    amount = it[Transactions.amount],
                    description = it[Transactions.description],
                    timestamp = it[Transactions.timestamp].toJavaInstant(),
                    id = it[Transactions.id].toID()
                )
            }
        }
    }
}