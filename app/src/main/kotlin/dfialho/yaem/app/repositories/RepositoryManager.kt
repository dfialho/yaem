package dfialho.yaem.app.repositories

import org.jetbrains.exposed.sql.Database

class ExposedRepositoryManager(dbConfig: DatabaseConfig) {

    init {
        Database.connect(dbConfig.url, dbConfig.driver)
    }

    private val accounts: ExposedAccountRepository by lazy { ExposedAccountRepository().apply { createTablesIfMissing() } }
    private val ledger: ExposedLedgerRepository by lazy { ExposedLedgerRepository(accounts).apply {
        accounts.createTablesIfMissing()
        createTablesIfMissing()
    } }

    fun getAccountRepository(): ExposedAccountRepository {
        return accounts
    }

    fun getLedgerRepository(): ExposedLedgerRepository {
        return ledger
    }
}