package dfialho.yaem.app.api

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val subCategories: List<String> = listOf()
) {
    companion object {
        const val NAME_MAX_LENGTH = 32
    }
}
