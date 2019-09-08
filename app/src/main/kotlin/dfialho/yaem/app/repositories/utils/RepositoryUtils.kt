package dfialho.yaem.app.repositories.utils

import dfialho.yaem.app.repositories.database.SQLExceptionTranslator
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException


fun <T> repositoryTransaction(translator: SQLExceptionTranslator, statement: Transaction.() -> T): T {

    try {
        return transaction { statement() }
    } catch (e : ExposedSQLException) {
        val cause = e.cause as? SQLException ?: throw e

        throw translator.translate(cause)
    }
}