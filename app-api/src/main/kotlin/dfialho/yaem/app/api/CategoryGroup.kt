package dfialho.yaem.app.api

data class CategoryGroup(
    val name: String,
    override val id: ID = randomID()
) : Resource
