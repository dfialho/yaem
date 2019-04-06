package dfialho.yaem.app.managers

import dfialho.yaem.app.Account
import dfialho.yaem.app.ID

interface AccountManager {

    fun create(account: Account)

    fun get(accountID: ID): Account

    fun list(): List<Account>

    fun update(accountID: String, account: Account)

    fun delete(accountID: String)
}
