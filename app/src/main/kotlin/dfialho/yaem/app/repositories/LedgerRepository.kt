package dfialho.yaem.app.repositories

import dfialho.yaem.app.ID
import dfialho.yaem.app.Transaction

interface LedgerRepository {

    fun create(transaction: Transaction): Transaction

    fun get(transactionID: ID): Transaction?

    fun list(): List<Transaction>

    fun exists(transactionID: ID): Boolean
}