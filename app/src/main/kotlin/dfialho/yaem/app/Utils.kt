package dfialho.yaem.app

import io.ktor.util.error
import org.slf4j.Logger

inline fun <R> logOnError(log: Logger, action: () -> R): R {
    try {
        return action()
    } catch (e: Exception) {
        log.error(e)
        throw e
    }
}
