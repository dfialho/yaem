package dfialho.yaem.app

import dfialho.yaem.app.exceptions.FoundException
import dfialho.yaem.app.managers.AccountManager
import dfialho.yaem.app.managers.AccountManagerImpl
import dfialho.yaem.app.repositories.AccountRepository
import dfialho.yaem.app.repositories.ExposedAccountRepository
import dfialho.yaem.app.validators.AccountValidator
import dfialho.yaem.app.validators.IDValidator
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.ValidationErrorException
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

    val accountRepository: AccountRepository = ExposedAccountRepository(
        url = "jdbc:h2:mem:$dbName;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver"
    )
    val accountManager: AccountManager = AccountManagerImpl(accountRepository, AccountValidator(IDValidator()))

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<ValidationErrorException> { cause ->
            call.respond(HttpStatusCode.BadRequest, Json.stringify(ValidationError.serializer().list, cause.errors))
        }

        exception<FoundException> { cause ->
            call.respond(HttpStatusCode.Conflict, cause.message.orEmpty())
        }
    }

    install(Routing) {
        get {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        route("api") {
            accounts(accountManager, log)
        }
    }
}

