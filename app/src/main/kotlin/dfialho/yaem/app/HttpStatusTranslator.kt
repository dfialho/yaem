package dfialho.yaem.app

import dfialho.yaem.app.validators.errors.ValidationError
import io.ktor.http.HttpStatusCode

class HttpStatusTranslator {

    fun translate(error: ValidationError): HttpStatusCode = when(error) {
        is ValidationError.NotFound -> HttpStatusCode.NotFound
        is ValidationError.MissingDependency -> HttpStatusCode.NotFound
        is ValidationError.NameExists -> HttpStatusCode.Conflict
        is ValidationError.References -> HttpStatusCode.Conflict
        else -> HttpStatusCode.BadRequest
    }
}
