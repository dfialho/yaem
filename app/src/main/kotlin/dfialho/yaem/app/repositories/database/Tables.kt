package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.api.ACCOUNT_NAME_MAX_LENGTH
import org.jetbrains.exposed.sql.Table

internal object Accounts : Table() {
    val id = uuid("ID").primaryKey()
    val name = varchar("NAME", length = ACCOUNT_NAME_MAX_LENGTH).uniqueIndex("NAME_INDEX")
    val initialBalance = double("INITIAL_BALANCE")
    val startTimestamp = datetime("START_TIMESTAMP")
}

internal object Transactions : Table() {
    val id = uuid("ID").primaryKey()
    val timestamp = datetime("TIMESTAMP")
    val amount = double("AMOUNT")
    val description = text("DESCRIPTION")
    val receiverAccount = uuid("RECEIVER_ACCOUNT") references Accounts.id
    val senderAccount = (uuid("SENDER_ACCOUNT") references Accounts.id).nullable()
}
