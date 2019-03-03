package dfialho.yaem.app

import java.time.Instant

fun randomTransaction(incomingAccount: ID, sendingAccount: ID? = null) = Transaction(
    amount = Math.random(),
    description = "random-trx-${randomID()}",
    incomingAccount = incomingAccount,
    timestamp = Instant.now(),
    sendingAccount = sendingAccount,
    id = randomID()
)
