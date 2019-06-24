package dfialho.yaem.app.api

import java.util.*

typealias ID = String

fun randomID(): ID {
    return UUID.randomUUID().toString()
}
