package dfialho.yaem.app.testutils

import assertk.Assert
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.support.fail
import dfialho.yaem.app.validators.errors.ValidationError
import dfialho.yaem.json.lib.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.list

inline fun <reified T> Assert<String?>.jsonListContainsAll(serializer: KSerializer<T>, vararg expectedItems: T) = given { actual ->
    actual?.let {
        val deserializedActual: List<T> = json.parse(serializer.list, actual)
        assertThat(deserializedActual).containsAll(*expectedItems)

    } ?: fail(emptyList<T>(), actual)
}

inline fun <reified T> Assert<String?>.isJsonEqualTo(serializer: KSerializer<T>, expected: T) = given { actual ->
    actual?.let {
        val deserializedActual: T = json.parse(serializer, actual)
        assertThat(deserializedActual).isEqualTo(expected)

    } ?: fail(emptyList<T>(), actual)
}

fun Assert<String?>.isErrorListWith(vararg errors: ValidationError) {
    val normalizedErrors = errors.map { it.toBaseError() }.toTypedArray()
    jsonListContainsAll(ValidationError.serializer(), *normalizedErrors)
}

private fun ValidationError.toBaseError(): ValidationError {
    return ValidationError(this.code, this.message)
}
