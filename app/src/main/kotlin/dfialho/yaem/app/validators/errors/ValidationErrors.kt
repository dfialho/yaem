package dfialho.yaem.app.validators.errors

import dfialho.yaem.app.api.ID

@Suppress("FunctionName")
object ValidationErrors{

    fun NotFound(resourceName: String, id: ID) = ValidationError.NotFound(
        code = "COMMON-01",
        resourceName = resourceName,
        resourceID = id
    )

    fun References(resourceName: String, id: ID) = ValidationError.References(
        code = "COMMON-02",
        resourceName = resourceName,
        resourceID = id
    )

    fun NameExists(resourceName: String, name: String) = ValidationError.NameExists(
        code = "COMMON-03",
        resourceName = resourceName,
        name = name
    )

    fun MissingDependency(resourceName: String, dependencyID: ID?) = ValidationError.MissingDependency(
        code = "COMMON-04",
        dependencyName = resourceName,
        dependencyID = dependencyID
    )
}

