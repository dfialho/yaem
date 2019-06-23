package dfialho.yaem.app.repositories

import org.jetbrains.exposed.sql.Database

class ExposedRepositoryManager(dbConfig: DatabaseConfig, translator: SQLExceptionTranslator = H2SQLExceptionTranslator()) {

    init {
        Database.connect(dbConfig.url, dbConfig.driver)
    }

    private val accounts: ExposedAccountRepository by lazy { ExposedAccountRepository(translator).apply { createTablesIfMissing() } }
    private val ledger: ExposedTransactionRepository by lazy { ExposedTransactionRepository(translator).apply {
        accounts.createTablesIfMissing()
        createTablesIfMissing()
    } }

    fun getAccountRepository(): ExposedAccountRepository {
        return accounts
    }

    fun getLedgerRepository(): ExposedTransactionRepository {
        return ledger
    }
}