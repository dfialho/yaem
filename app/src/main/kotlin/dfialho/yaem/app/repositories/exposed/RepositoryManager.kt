package dfialho.yaem.app.repositories.exposed

import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.repositories.exceptions.H2SQLExceptionTranslator
import dfialho.yaem.app.repositories.exceptions.SQLExceptionTranslator
import org.jetbrains.exposed.sql.Database

class ExposedRepositoryManager(dbConfig: DatabaseConfig, translator: SQLExceptionTranslator = H2SQLExceptionTranslator()) {

    init {
        Database.connect(dbConfig.url, dbConfig.driver)
    }

    private val accounts: ExposedAccountRepository by lazy { ExposedAccountRepository(
        translator
    ).apply { createTablesIfMissing() } }
    private val ledger: ExposedTransactionRepository by lazy { ExposedTransactionRepository(
        translator
    ).apply {
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