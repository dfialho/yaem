package dfialho.yaem.app

import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import io.ktor.application.ApplicationCall
import io.ktor.request.receive

suspend inline fun <reified T : Any> ApplicationCall.validatedReceive(): T {
    try {
        return receive()
    } catch (e: Exception) {
        throwError(e) { ValidationError.InvalidJson(T::class.simpleName.orEmpty()) }
    }
}
