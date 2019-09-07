package dfialho.yaem.app

import dfialho.yaem.app.api.*
import java.time.Instant

fun anyTransaction(account: ID, sender: ID? = null) = Transaction(
    amount = Math.random(),
    receiver = account,
    sender = sender,
    description = "random-trx-${randomID()}",
    timestamp = Instant.now(),
    id = randomID()
)
