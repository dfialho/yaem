package dfialho.yaem.app.repositories

import org.h2.api.ErrorCode
import java.sql.SQLException

class H2SQLExceptionTranslator : SQLExceptionTranslator {

    override fun translate(exception: SQLException): DatabaseException = when (exception.errorCode) {
        ErrorCode.DUPLICATE_KEY_1 -> DuplicateKeyException(exception)
        ErrorCode.REFERENTIAL_INTEGRITY_VIOLATED_PARENT_MISSING_1 -> ParentMissingException(exception)
        ErrorCode.REFERENTIAL_INTEGRITY_VIOLATED_CHILD_EXISTS_1 -> ChildExistsException(exception)
        else -> UnknownDatabaseException(exception, "Unexpected database exception (code=${exception.errorCode})")
    }
}