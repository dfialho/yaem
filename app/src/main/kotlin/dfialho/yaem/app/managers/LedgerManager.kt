package dfialho.yaem.app.managers

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction

interface LedgerManager {

    fun create(transaction: Transaction): Transaction

    fun get(transactionID: ID): Transaction?

    fun list(): List<Transaction>
}