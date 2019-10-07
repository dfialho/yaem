package dfialho.yaem.app.testutils.resources

import dfialho.yaem.app.api.Account
import dfialho.yaem.app.api.Category
import dfialho.yaem.app.api.SubCategory
import dfialho.yaem.app.api.Transaction
import kotlin.reflect.KClass

val apis = mapOf<KClass<*>, String>(
    Account::class to "/api/accounts",
    Transaction::class to "/api/transactions",
    Category::class to "/api/categories",
    SubCategory::class to "/api/categories/sub"
)

inline fun <reified T : Any> api(): String {
    return apis[T::class] ?: throw IllegalArgumentException("There is no API URL defined for ${T::class.simpleName}")
}
