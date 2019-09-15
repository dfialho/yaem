package dfialho.yaem.app.testutils

import dfialho.yaem.app.api.*
import java.time.Instant
import java.util.*

fun anyAccount(): Account {
    return Account(
        name = "Acc-${UUID.randomUUID().toString().substring(0, 5)}",
        initialBalance = Math.random()
    )
}

fun anyTransaction(account: ID, sender: ID? = null): Transaction = Transaction(
    amount = Math.random(),
    receiver = account,
    sender = sender,
    description = "random-trx-${randomID()}",
    timestamp = Instant.now(),
    id = randomID()
)
