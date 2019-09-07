package dfialho.yaem.app.testutils

import dfialho.yaem.app.api.Account

class AccountWithoutId(account: Account) {

    private val account = account.copy(id = "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountWithoutId

        return account == other.account.copy(id = account.id)
    }

    override fun hashCode(): Int {
        return account.hashCode()
    }
}

fun Account.ignoreId(): AccountWithoutId {
    return AccountWithoutId(this)
}
