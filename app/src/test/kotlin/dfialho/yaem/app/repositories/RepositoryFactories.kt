package dfialho.yaem.app.repositories

import dfialho.yaem.app.repositories.exposed.ExposedRepositoryManager
import java.util.*


fun uniqueRepositoryManager(): ExposedRepositoryManager {
    val dbName = UUID.randomUUID().toString()
    return ExposedRepositoryManager(
        DatabaseConfig(
            url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        )
    )
}
