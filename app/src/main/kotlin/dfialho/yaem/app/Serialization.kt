package dfialho.yaem.app

import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.throwError
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat

inline fun <reified T> StringFormat.validatedParse(deserializer: DeserializationStrategy<T>, string: String): T {

    return try {
        parse(deserializer, string)
    } catch (e: Exception) {
        throwError { ValidationError.InvalidJson(T::class.simpleName.orEmpty()) }
    }
}
