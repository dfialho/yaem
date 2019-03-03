package dfialho.yaem.app

import dfialho.yaem.app.managers.AccountManager
import dfialho.yaem.app.managers.AccountManagerImpl
import dfialho.yaem.app.managers.LedgerManager
import dfialho.yaem.app.managers.LedgerManagerImpl
import dfialho.yaem.app.repositories.*
import dfialho.yaem.app.validators.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.slf4j.event.Level
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    val dbName = if (testing) UUID.randomUUID().toString() else "prod"

    val repositoryManager = ExposedRepositoryManager(
        DatabaseConfig(
            url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        )
    )
    val accountRepository: AccountRepository = repositoryManager.getAccountRepository()
    val accountManager: AccountManager = AccountManagerImpl(accountRepository, AccountValidator(IDValidator()))
    val ledgerRepository: LedgerRepository = repositoryManager.getLedgerRepository()
    val ledgerManager: LedgerManager = LedgerManagerImpl(ledgerRepository, TransactionValidator(IDValidator()))

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<ValidationErrorException> { cause ->
            val errors = cause.errors

            val statusCode = when {
                errors.size == 1 -> when (errors[0]) {
                    is ValidationError.NotFound -> HttpStatusCode.NotFound
                    is ValidationError.AccountReferences -> HttpStatusCode.Conflict
                    else -> HttpStatusCode.BadRequest
                }
                else -> HttpStatusCode.BadRequest
            }

            call.respond(statusCode, Json.stringify(ValidationError.serializer().list, errors))
        }

        exception<DuplicateKeyException> { cause ->
            call.respond(HttpStatusCode.Conflict, cause.message.orEmpty())
        }
    }

    install(Routing) {
        get {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        route("api") {
            accounts(accountManager, log)
            ledger(ledgerManager, log)
        }
    }
}

