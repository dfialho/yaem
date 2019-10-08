package dfialho.yaem.app.api

data class Category(
    val name: String,
    val group: ID,
    override val id: ID = randomID()
) : Resource {

    companion object {
        const val NAME_MAX_LENGTH = 32
    }
}
