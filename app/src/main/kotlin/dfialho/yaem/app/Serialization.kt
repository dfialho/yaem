package dfialho.yaem.app

import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

val json = Json(JsonConfiguration.Stable)

inline fun <reified T> StringFormat.validatedParse(deserializer: DeserializationStrategy<T>, string: String): T {

    return try {
        parse(deserializer, string)
    } catch (e: Exception) {
        throwError(e) { ValidationError.InvalidJson(T::class.simpleName.orEmpty()) }
    }
}
