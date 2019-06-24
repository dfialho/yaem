package dfialho.yaem.app

import dfialho.yaem.app.api.ID
import dfialho.yaem.app.api.OneWayTransaction
import dfialho.yaem.app.api.Transfer
import dfialho.yaem.app.api.randomID
import java.time.Instant

fun randomOneWayTransaction(account: ID) = OneWayTransaction(
    account = account,
    amount = Math.random(),
    description = "random-trx-${randomID()}",
    timestamp = Instant.now(),
    id = randomID()
)

fun randomTransfer(incomingAccount: ID, outgoingAccount: ID) = Transfer(
    outgoingAccount = outgoingAccount,
    incomingAccount = incomingAccount,
    amount = Math.random(),
    description = "random-trx-${randomID()}",
    timestamp = Instant.now(),
    id = randomID()
)
