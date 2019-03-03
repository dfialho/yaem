package dfialho.yaem.app.repositories

import java.sql.SQLException

sealed class DatabaseException(cause: SQLException?, message: String? = null) : Exception(message, cause)

class DuplicateKeyException(cause: SQLException, message: String? = null) : DatabaseException(cause, message)
class ParentMissingException(cause: SQLException, message: String? = null) : DatabaseException(cause, message)
class ChildExistsException(cause: SQLException, message: String? = null) : DatabaseException(cause, message)
class UnknownDatabaseException(cause: SQLException, message: String? = null) : DatabaseException(cause, message)
class NotFoundException(message: String? = null) : DatabaseException(null, message)
