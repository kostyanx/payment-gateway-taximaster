package ru.catcab.taximaster.paymentgateway.controller

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.util.pipeline.*
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbRequest
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbResponseError
import ru.catcab.taximaster.paymentgateway.dto.sberbank.ResponseError
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankPaymentOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.containsAddress

class CcbController(
    private val config: ApplicationConfig,
    private val sberbankCheckOperation: SberbankCheckOperation,
    private val sberbankPaymentOperation: SberbankPaymentOperation,
) {
    suspend fun activate(pipelineContext: PipelineContext<Unit, ApplicationCall>, request: CcbRequest): Any {
        return pipelineContext.apply {
            val remoteHost = call.request.origin.remoteHost
            val allowed =
                config.sberbank.security.allowedHosts.contains(remoteHost) || config.sberbank.security.allowedSubnets.any { it.containsAddress(remoteHost) }
            if (!allowed) return CcbResponseError(10, "Запрос выполнен с неразрешенного адреса")

            val action = request.params.act
            val account = request.params.account
            val servCode = request.params.servCode

            when (action) {
                CcbRequest.ACTION_CHECK -> {
                    if (servCode.let { it != null && !config.ccb.allowedServValues.contains(it) })
                        return CcbResponseError(3, SberbankCheckOperation.ACCOUNT_NOT_FOUND)
                    sberbankCheckOperation.activate(account)
                }
                CcbRequest.ACTION_PAYMENT -> {
                    val amount = request.params.payAmount!!
                    val payId = request.params.payId!!
                    val payDate = request.params.payDate
//                    sberbankPaymentOperation.activate(account, amount, payId, payDate)
                }
                else -> {
                    ResponseError(2, "Неизвестный тип запроса")
                }
            }
        }
    }
}