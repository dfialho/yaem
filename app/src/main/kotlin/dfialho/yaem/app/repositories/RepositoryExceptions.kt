package dfialho.yaem.app.repositories

sealed class RepositoryException(cause: Exception?, message: String? = null) : Exception(message, cause)

class DuplicateKeyException(cause: Exception, message: String? = null) : RepositoryException(cause, message)
class ParentMissingException(cause: Exception, message: String? = null) : RepositoryException(cause, message)
class ChildExistsException(cause: Exception, message: String? = null) : RepositoryException(cause, message)
class UnknownRepositoryException(cause: Exception, message: String? = null) : RepositoryException(cause, message)
class NotFoundException(message: String? = null) : RepositoryException(null, message)
