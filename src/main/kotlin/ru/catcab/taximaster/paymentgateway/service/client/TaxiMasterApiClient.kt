package ru.catcab.taximaster.paymentgateway.service.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.dto.api.enums.OperType
import ru.catcab.taximaster.paymentgateway.dto.api.queries.*
import ru.catcab.taximaster.paymentgateway.exception.TaxiMasterApiClientException
import ru.catcab.taximaster.paymentgateway.stub.TrustAllX509TrustManager
import ru.catcab.taximaster.paymentgateway.util.ktor.feature.TaxiMasterAuth

open class TaxiMasterApiClient(
    baseUrl: String,
    secret: String
) {
    companion object {
        private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
        private val snakeRegex = "_[a-zA-Z]".toRegex()

        // String extensions
        fun String.camelToSnakeCase(): String {
            return camelRegex.replace(this) {
                "_${it.value}"
            }.toLowerCase()
        }

        fun String.snakeToLowerCamelCase(): String {
            return snakeRegex.replace(this) {
                it.value.replace("_","")
                    .toUpperCase()
            }
        }

        fun String.snakeToUpperCamelCase(): String {
            return this.snakeToLowerCamelCase().capitalize()
        }
    }

    private val client = HttpClient(CIO) {
        engine {
            https {
                trustManager = TrustAllX509TrustManager()
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                private val delegate = LoggerFactory.getLogger(HttpClient::class.java)!!
                override fun log(message: String) = delegate.trace(message)
            }
            level = LogLevel.ALL
        }
        install(TaxiMasterAuth) {
            this.secret = secret
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 10_000
        }
    }

    private val baseUrl = baseUrl.removeSuffix("/")

    private suspend inline fun <reified RQ: Any, reified RS> sendRequest(request: RQ): RS {
        val requestName = RQ::class.java.simpleName.removeSuffix("Request").camelToSnakeCase()
        return client.post("$baseUrl/$requestName") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    private inline fun <reified T : CommonResponse<*>> checkResponse(response: T): T {
        if (response.code != 0) {
            throw TaxiMasterApiClientException(response.code, response.descr, response)
        }
        return response
    }

    suspend fun createDriverOperation(
        driverId: Int,
        operSum: Double,
        operType: OperType,
        name: String? = null,
        comment: String? = null,
        accountKind: Int = 0
    ): CreateDriverOperationResponse {
        val request = CreateDriverOperationRequest(driverId, operSum, operType, name, comment, accountKind)
        val response = sendRequest<CreateDriverOperationRequest, CreateDriverOperationResponse>(request)
        return checkResponse(response)
    }

    suspend fun getCrewsInfo(): GetCrewsInfoResponse {
        val response = client.get<GetCrewsInfoResponse>("$baseUrl/get_crews_info")
        return checkResponse(response)
    }

    suspend fun getCrewInfo(id: Int): GetCrewInfoResponse {
        val response = client.get<GetCrewInfoResponse>("$baseUrl/get_crew_info") {
            parameter("crew_id", id)
        }
        return checkResponse(response)
    }

    suspend fun getDriverInfo(id: Int): GetDriverInfoResponse {
        val response = client.get<GetDriverInfoResponse>("$baseUrl/get_driver_info") {
            parameter("driver_id", id)
        }
        return checkResponse(response)
    }

    suspend fun getDriversInfo(lockedDrivers: Boolean = false, dismissedDrivers: Boolean = false): GetDriversInfoResponse {
        val response = client.get<GetDriversInfoResponse>("$baseUrl/get_drivers_info") {
            parameter("locked_drivers", lockedDrivers)
            parameter("dismissed_drivers", dismissedDrivers)
        }
        return checkResponse(response)
    }


}