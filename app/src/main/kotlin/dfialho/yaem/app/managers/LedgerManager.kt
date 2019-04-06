package dfialho.yaem.app.managers

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction

interface LedgerManager {

    fun create(transaction: Transaction)

    fun get(transactionID: ID): Transaction

    fun list(): List<Transaction>

    fun update(trxID: String, trx: Transaction)

    fun delete(transactionID: String)
}