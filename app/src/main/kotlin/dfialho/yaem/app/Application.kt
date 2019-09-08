package dfialho.yaem.app

import dfialho.yaem.app.controllers.AccountController
import dfialho.yaem.app.controllers.TransactionController
import dfialho.yaem.app.repositories.*
import dfialho.yaem.app.repositories.database.DatabaseRepositoryManager
import dfialho.yaem.app.repositories.DuplicateKeyException
import dfialho.yaem.app.validators.*
import dfialho.yaem.json.lib.json
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.serialization
import kotlinx.serialization.list
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.app() {
    app(
        DatabaseConfig(
            url = "jdbc:h2:mem:yaem;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            driver = "org.h2.Driver"
        )
    )
}

fun Application.app(dbConfig: DatabaseConfig) {

    val repositoryManager = DatabaseRepositoryManager(dbConfig)
    val accountRepository: AccountRepository = repositoryManager.getAccountRepository()
    val accountController = AccountController(accountRepository, AccountValidator())
    val transactionRepository: TransactionRepository = repositoryManager.getLedgerRepository()
    val transactionController = TransactionController(transactionRepository, TransactionValidator())

    install(DefaultHeaders)
    install(ContentNegotiation) {
        serialization(json = json)
    }
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

            call.respondText(ContentType.Application.Json, statusCode) {
                json.stringify(ValidationError.serializer().list, errors)
            }
        }

        exception<DuplicateKeyException> { cause ->
            call.respond(HttpStatusCode.Conflict, cause.message.orEmpty())
        }
    }
    // Must be installed after StatusPages
    // StatusPages absorbs the exceptions this feature requires to log the errors
    install(ErrorLogging)

    routing {
        route("api") {
            accounts(accountController)
            ledger(transactionController)
        }
    }
}

