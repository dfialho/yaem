package dfialho.yaem.app.repositories.database

import dfialho.yaem.app.repositories.DatabaseConfig
import org.jetbrains.exposed.sql.Database

class DatabaseRepositoryManager(dbConfig: DatabaseConfig, translator: SQLExceptionTranslator = H2SQLExceptionTranslator()) {

    init {
        Database.connect(dbConfig.url, dbConfig.driver)
    }

    private val accounts: DatabaseAccountRepository by lazy { DatabaseAccountRepository(
        translator
    ).apply { createTablesIfMissing() } }

    fun getAccountRepository(): DatabaseAccountRepository {
        return accounts
    }
}