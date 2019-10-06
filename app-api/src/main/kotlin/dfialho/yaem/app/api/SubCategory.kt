package dfialho.yaem.app.api

import kotlinx.serialization.Serializable

@Serializable
data class SubCategory(
    val category: String,
    val name: String
)
