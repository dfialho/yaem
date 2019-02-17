package dfialho.yaem.app.managers

import dfialho.yaem.app.Account
import dfialho.yaem.app.ID
import dfialho.yaem.app.Result

interface AccountManager {

    fun create(account: Account): Account

    fun get(accountID: ID): Account?

    fun list(): List<Account>

    fun delete(accountID: String): Result
}