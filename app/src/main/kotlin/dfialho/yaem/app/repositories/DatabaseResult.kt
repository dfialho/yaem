package dfialho.yaem.app.repositories

sealed class DatabaseResult {
    object Success : DatabaseResult()
    class Failure(val exception: Exception) : DatabaseResult()
    object DuplicateKey : DatabaseResult()
}

fun DatabaseResult.onDuplicateKey(action: () -> Unit): DatabaseResult {
    if (this == DatabaseResult.DuplicateKey) {
        action()
    }

    return this
}

fun DatabaseResult.onFailureThrowException(): DatabaseResult {
    if (this is DatabaseResult.Failure) {
        throw this.exception
    }

    return this
}
