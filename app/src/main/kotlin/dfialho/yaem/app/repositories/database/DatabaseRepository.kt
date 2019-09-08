package dfialho.yaem.app.repositories.database

/**
 * Base interface for a repository implemented using the exposed library.
 */
internal interface DatabaseRepository {
    fun createTablesIfMissing()
}