package dfialho.yaem.app.repositories

import dfialho.yaem.app.Account
import dfialho.yaem.app.ID

/**
 * Repository holding Accounts.
 */
interface AccountRepository {

    fun create(account: Account)

    fun get(accountID: ID): Account?

    fun list(): List<Account>

    fun exists(accountID: ID): Boolean

    fun update(accountID: String, account: Account)

    fun delete(accountID: String)
}