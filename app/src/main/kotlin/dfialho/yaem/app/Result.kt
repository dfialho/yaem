package dfialho.yaem.app

sealed class Result {

    object Success : Result() {
        override fun toString(): String = "Success"
    }

    object Failure : Result() {
        override fun toString(): String = "Failure"
    }
}