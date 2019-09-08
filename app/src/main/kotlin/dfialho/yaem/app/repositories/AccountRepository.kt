package dfialho.yaem.app.repositories

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.ID

/**
 * Repository holding Accounts.
 */
interface AccountRepository {

    fun create(account: Account)

    fun get(accountID: ID): Account

    fun list(): List<Account>

    fun exists(accountID: ID): Boolean

    fun update(account: Account)

    fun delete(accountID: String)
}