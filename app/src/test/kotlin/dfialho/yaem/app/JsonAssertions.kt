package dfialho.yaem.app

import assertk.Assert
import assertk.assertions.*
import assertk.assertions.support.fail
import dfialho.yaem.app.validators.ValidationError
import dfialho.yaem.app.validators.toBaseError
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

inline fun <reified T> Assert<String?>.isJsonEmptyList(serializer: KSerializer<T>) = given { actual ->
    actual?.let {
        val deserializedActual: List<T> = Json.parse(serializer.list, actual)
        assertThat(deserializedActual).isEmpty()

    } ?: fail(emptyList<T>(), actual)
}

inline fun <reified T> Assert<String?>.jsonListContainsExactly(serializer: KSerializer<T>, vararg expectedItems: T) = given { actual ->
    actual?.let {
        val deserializedActual: List<T> = Json.parse(serializer.list, actual)
        assertThat(deserializedActual).containsExactly(*expectedItems)

    } ?: fail(emptyList<T>(), actual)
}

inline fun <reified T> Assert<String?>.jsonListContainsAll(serializer: KSerializer<T>, vararg expectedItems: T) = given { actual ->
    actual?.let {
        val deserializedActual: List<T> = Json.parse(serializer.list, actual)
        assertThat(deserializedActual).containsAll(*expectedItems)

    } ?: fail(emptyList<T>(), actual)
}

inline fun <reified T> Assert<String?>.jsonListContainsOnly(serializer: KSerializer<T>, vararg expectedItems: T) = given { actual ->
    actual?.let {
        val deserializedActual: List<T> = Json.parse(serializer.list, actual)
        assertThat(deserializedActual).containsOnly(*expectedItems)

    } ?: fail(emptyList<T>(), actual)
}

inline fun <reified T> Assert<String?>.isJsonEqualTo(serializer: KSerializer<T>, expected: T) = given { actual ->
    actual?.let {
        val deserializedActual: T = Json.parse(serializer, actual)
        assertThat(deserializedActual).isEqualTo(expected)

    } ?: fail(emptyList<T>(), actual)
}

fun Assert<String?>.errorListContainsAll(vararg errors: ValidationError) {
    val normalizedErrors = errors.map { it.toBaseError() }.toTypedArray()
    jsonListContainsAll(ValidationError.serializer(), *normalizedErrors)
}
