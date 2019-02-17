package dfialho.yaem.app

sealed class DeleteResult {
    object Success : DeleteResult()
    object NotFound : DeleteResult()
    object ChildExists : DeleteResult()
}