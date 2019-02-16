package dfialho.yaem.app

import java.util.*

typealias ID = String

fun randomID(): ID {
    return UUID.randomUUID().toString()
}
