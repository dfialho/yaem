package dfialho.yaem.app.repositories.exposed

/**
 * Base interface for a repository implemented using the exposed library.
 */
internal interface ExposedRepository {
    fun createTablesIfMissing()
}