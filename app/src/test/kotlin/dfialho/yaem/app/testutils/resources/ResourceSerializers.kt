package dfialho.yaem.app.testutils.resources

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory
import dfialho.yaem.app.api.Transaction
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

val serializers = mapOf<KClass<*>, KSerializer<*>>(
    Account::class to Account.serializer(),
    Transaction::class to Transaction.serializer(),
    Category::class to Category.serializer(),
    SubCategory::class to SubCategory.serializer()
)

fun <T : Any> T.serializer(): KSerializer<T> {
    @Suppress("UNCHECKED_CAST")
    return serializers[this::class] as KSerializer<T>?
        ?: throw IllegalArgumentException("There is no serializer defined for ${this::class.simpleName}")
}

inline fun <reified T : Any> serializer(): KSerializer<T> {
    @Suppress("UNCHECKED_CAST")
    return serializers[T::class] as KSerializer<T>?
        ?: throw IllegalArgumentException("There is no serializer defined for ${T::class.simpleName}")
}
