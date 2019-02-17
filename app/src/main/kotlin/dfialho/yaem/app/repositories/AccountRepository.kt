package dfialho.yaem.app.repositories

import dfialho.yaem.app.Account
import dfialho.yaem.app.ID

interface AccountRepository {

    fun create(account: Account): Account

    fun get(accountID: ID): Account?

    fun list(): List<Account>

    fun exists(accountID: ID): Boolean
}