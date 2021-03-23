package ru.catcab.taximaster.paymentgateway.controller

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.util.pipeline.*
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbRequest
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbResponseError
import ru.catcab.taximaster.paymentgateway.logic.CcbCheckOperation
import ru.catcab.taximaster.paymentgateway.logic.CcbPaymentOperation
import ru.catcab.taximaster.paymentgateway.logic.SberbankCheckOperation
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.ccbSignatureIsValid
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.containsAddress

class CcbController(
    private val config: ApplicationConfig,
    private val ccbCheckOperation: CcbCheckOperation,
    private val ccbPaymentOperation: CcbPaymentOperation,
) {
    companion object {
        @JvmStatic private val NOT_ALL_REQUIRED_PARAMETERS_IS_SET = "Указаны не все необходимые параметры"
        @JvmStatic private val INVALID_SIGNATURE = "Неверная цифровая подпись"
    }

    suspend fun activate(pipelineContext: PipelineContext<Unit, ApplicationCall>, xmlString: String, request: CcbRequest): Any {
        pipelineContext.apply {
            val remoteHost = call.request.origin.remoteHost
            val allowed = config.ccb.allowedHosts.contains(remoteHost) || config.ccb.allowedSubnets.any { it.containsAddress(remoteHost) }
            if (!allowed) return CcbResponseError(10, "Запрос выполнен с неразрешенного адреса")

            if (config.ccb.checkSign && !ccbSignatureIsValid(xmlString, "params", "sign", config.ccb.secret))
                return CcbResponseError(13, INVALID_SIGNATURE)

            val action = request.params.act ?: return CcbResponseError(11, NOT_ALL_REQUIRED_PARAMETERS_IS_SET)
            val account = request.params.account ?: return CcbResponseError(11, NOT_ALL_REQUIRED_PARAMETERS_IS_SET)
            val servCode = request.params.servCode

            return when (action) {
                CcbRequest.ACTION_CHECK -> {
                    if (servCode.let { it != null && !config.ccb.allowedServValues.contains(it) })
                        return CcbResponseError(3, SberbankCheckOperation.ACCOUNT_NOT_FOUND)
                    ccbCheckOperation.activate(account)
                }
                CcbRequest.ACTION_PAYMENT -> {
                    val amount = request.params.payAmount ?: return CcbResponseError(11, NOT_ALL_REQUIRED_PARAMETERS_IS_SET)
                    val payId = request.params.payId ?: return CcbResponseError(11, NOT_ALL_REQUIRED_PARAMETERS_IS_SET)
                    val payDate = request.params.payDate ?: return CcbResponseError(11, NOT_ALL_REQUIRED_PARAMETERS_IS_SET)
                    ccbPaymentOperation.activate(account, amount, payId, payDate)
                }
                else -> {
                    CcbResponseError(12, "Неверный формат параметров")
                }
            }
        }
    }
}