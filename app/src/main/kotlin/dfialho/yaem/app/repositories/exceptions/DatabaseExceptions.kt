package dfialho.yaem.app.repositories.exceptions

sealed class DatabaseException(cause: Exception?, message: String? = null) : Exception(message, cause)

class DuplicateKeyException(cause: Exception, message: String? = null) : DatabaseException(cause, message)
class ParentMissingException(cause: Exception, message: String? = null) : DatabaseException(cause, message)
class ChildExistsException(cause: Exception, message: String? = null) : DatabaseException(cause, message)
class UnknownDatabaseException(cause: Exception, message: String? = null) : DatabaseException(cause, message)
class NotFoundException(message: String? = null) : DatabaseException(null, message)
