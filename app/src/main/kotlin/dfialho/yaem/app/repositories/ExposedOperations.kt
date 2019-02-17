package dfialho.yaem.app.repositories

import org.h2.api.ErrorCode
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.sql.SQLException

/**
 * Inserts a new element into a [Table].
 * The output of this method is as follows:
 * - Returns [DatabaseResult.Success] if the insert operation is successful
 * - Returns [DatabaseResult.Failure] if the insert operation fails because the table already includes the
 *   element being inserted (duplicate key error).
 * - Throws [ExposedSQLException] if some other error occurs besides a "duplicate key error".
 *
 * WARNING: At this point this operation is only supported for H2 databases. Please update it if we
 * need to support other SQL databases.
 */
fun <T : Table> T.insertUnique(body: T.(InsertStatement<Number>) -> Unit): DatabaseResult {
    try {
        insert(body)
        return DatabaseResult.Success

    } catch (e: ExposedSQLException) {
        val sqlException = e.cause as SQLException

        // WARNING: Error codes are implementation specific
        // These codes only apply to the H2 driver

        return when (sqlException.errorCode) {
            ErrorCode.DUPLICATE_KEY_1 -> DatabaseResult.DuplicateKey
            ErrorCode.REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1 -> DatabaseResult.ParentMissing
            else -> DatabaseResult.Failure(e)
        }
    }
}

fun <T : Table, R> T.translateSQLExceptions(block: T.() -> R): R {

    try {
        return block()
    } catch (e: ExposedSQLException) {
        val sqlException = e.cause as SQLException

        // WARNING: Error codes are implementation specific
        // These codes only apply to the H2 driver

        when (sqlException.errorCode) {
            ErrorCode.DUPLICATE_KEY_1 -> throw DuplicateKeyException(sqlException)
            ErrorCode.REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1 -> throw ParentMissingException(sqlException)
            ErrorCode.REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1 -> throw ChildExistsException(sqlException)
            else -> throw UnknownDatabaseException(sqlException)
        }
    }
}