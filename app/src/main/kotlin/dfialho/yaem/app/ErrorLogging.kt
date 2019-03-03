package dfialho.yaem.app

import io.ktor.application.*
import io.ktor.util.AttributeKey
import io.ktor.util.error
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger

class ErrorLogging private constructor(private val log: Logger) {

    /**
     * Configuration for [ErrorLogging] feature
     */
    class Configuration {

        /**
         * Customize [Logger], will default to [ApplicationEnvironment.log]
         */
        var logger: Logger? = null
    }

    companion object Feature : ApplicationFeature<Application, Configuration, ErrorLogging> {
        override val key = AttributeKey<ErrorLogging>("Error Logging")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): ErrorLogging {

            val configuration = Configuration().apply(configure)

            val feature = ErrorLogging(log = configuration.logger ?: pipeline.log)

            pipeline.intercept(ApplicationCallPipeline.Monitoring) { feature.interceptCall(this) }
            return feature
        }
    }

    private suspend fun interceptCall(context: PipelineContext<Unit, ApplicationCall>) {
        try {
            coroutineScope {
                context.proceed()
            }
        } catch (exception: Throwable) {
            log.error(exception)
            throw exception
        }
    }
}
