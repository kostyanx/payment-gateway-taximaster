package ru.catcab.taximaster.paymentgateway.logic

import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.configuration.xmlMapper
import ru.catcab.taximaster.paymentgateway.database.entity.RequestLog
import ru.catcab.taximaster.paymentgateway.database.enum.RequestMethod.Companion.toRequestMethod
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey

class RequestLogOperation(
    private val database: Database
) {
    suspend fun activate(method: HttpMethod, path: String, params: Parameters, responseCode: Int, response: Any) {
        withContext(Dispatchers.IO) {
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