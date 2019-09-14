package dfialho.yaem.app.testutils

import dfialho.yaem.app.repositories.DatabaseConfig
import dfialho.yaem.app.repositories.database.DatabaseRepositoryManager
import dfialho.yaem.app.repositories.database.H2SQLExceptionTranslator
import java.util.*


fun uniqueRepositoryManager(): DatabaseRepositoryManager {
    val dbName = UUID.randomUUID().toString()

    return DatabaseRepositoryManager(
        dbConfig = DatabaseConfig(
            url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        ),
        translator = H2SQLExceptionTranslator()
    )
}
