package dfialho.yaem.app.repositories

sealed class DatabaseResult {
    object Success : DatabaseResult()
    class Failure(val exception: Exception) : DatabaseResult()
    object DuplicateKey : DatabaseResult()
    object ParentMissing : DatabaseResult()
}

fun DatabaseResult.applyOnDuplicateKey(action: () -> Unit) = applyOnExpectedResult(DatabaseResult.DuplicateKey, action)

fun DatabaseResult.applyOnParentMissing(action: () -> Unit) = applyOnExpectedResult(DatabaseResult.ParentMissing, action)

private fun DatabaseResult.applyOnExpectedResult(expected: DatabaseResult, action: () -> Unit): DatabaseResult {
    if (this == expected) {
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
