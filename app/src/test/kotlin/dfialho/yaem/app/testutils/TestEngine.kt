package dfialho.yaem.app.testutils

import dfialho.yaem.app.app
import dfialho.yaem.app.repositories.DatabaseConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import java.util.*

fun <R> withTestResourceAPI(test: TestApplicationEngine.() -> R): R {

    val dbConfig = DatabaseConfig(
        url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )

    return withTestApplication({ app(dbConfig) }, test)
}
