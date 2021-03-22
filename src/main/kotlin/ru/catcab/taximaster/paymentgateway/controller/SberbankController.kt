package ru.catcab.taximaster.paymentgateway.controller

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.dto.sberbank.ResponseError
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankPaymentOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.containsAddress

class SberbankController(
    private val config: ApplicationConfig,
    private val sberbankCheckOperation: SberbankCheckOperation,
    private val sberbankPaymentOperation: SberbankPaymentOperation,
) {
    suspend fun activate(pipelineContext: PipelineContext<Unit, ApplicationCall>, params: Parameters): Any {
        pipelineContext.apply {
            val remoteHost = call.request.origin.remoteHost
            val allowed = config.sberbank.security.allowedHosts.contains(remoteHost) || config.sberbank.security.allowedSubnets.any { it.containsAddress(remoteHost) }
            if (!allowed) return ResponseError(2, "Запрос выполнен с неразрешенного адреса")

            val action = requireNotNull(params["ACTION"], { "ACTION parameter not defined" })
            val account = requireNotNull(params["ACCOUNT"], { "ACCOUNT parameter not defined" })
            val response: Any = when (action.toLowerCase()) {
                "check" -> {
                    if (params["SERV"].let { it != null && !config.sberbank.allowedServValues.contains(it) })
                        ResponseError(3, SberbankCheckOperation.ACCOUNT_NOT_FOUND)
                    else
                        sberbankCheckOperation.activate(account)
                }
                "payment" -> {
                    val amount = requireNotNull(params["AMOUNT"], { "AMOUNT parameter not defined" })
                    val payId = requireNotNull(params["PAY_ID"], { "PAY_ID parameter not defined" })
                    val payDate = requireNotNull(params["PAY_DATE"], { "PAY_DATE parameter not defined" })
                    val payCh = params["PAY_CH"]
                    sberbankPaymentOperation.activate(account, amount, payId, payDate, payCh)
                }
                else -> {
                    ResponseError(2, "Неизвестный тип запроса")
                }
            }
            return response
        }
    }
}