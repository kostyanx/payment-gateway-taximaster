package ru.catcab.taximaster.paymentgateway.logic

import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.Application
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.configuration.xmlMapper
import ru.catcab.taximaster.paymentgateway.database.entity.RequestLog
import ru.catcab.taximaster.paymentgateway.database.enum.RequestMethod.Companion.toRequestMethod
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey

class RequestLogOperation(
    private val config: ApplicationConfig,
    private val database: Database
) {
    companion object {
        @Suppress("EXPERIMENTAL_API_USAGE")
        @JvmStatic val AUDIT_LOG = LoggerFactory.getLogger(Application::class.java.packageName + ".audit")!!
    }

    suspend fun activate(method: HttpMethod, path: String, params: Parameters, responseCode: Int, response: Any) {
        withContext(Dispatchers.IO) {
            params["ACTION"]
                ?.let { action -> config.logging.audit.templates.firstOrNull { it.action == action && path.startsWith(it.path) } }
                ?.resolve(params::get)
                ?.also(AUDIT_LOG::info)

            val parametersString = params.formUrlEncode()
            @Suppress("BlockingMethodInNonBlockingContext")
            val responseString = xmlMapper.writeValueAsString(response)
            println("path: $path, parameters: $parametersString, response: $responseString")
            transaction(database) {
                RequestLog.insert {
                    it[requestId] = MDCKey.getValue(MDCKey.REQUEST_ID)
                    it[requestMethod] = method.toRequestMethod()
                    it[requestUrl] = path
                    it[requestBody] = parametersString
                    it[this.responseCode] = responseCode
                    it[responseBody] = responseString
                }
            }
        }
    }
}