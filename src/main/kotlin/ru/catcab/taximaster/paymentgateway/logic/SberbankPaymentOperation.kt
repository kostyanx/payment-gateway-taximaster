package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.SBERBANK_PAYMENT
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator

class SberbankPaymentOperation(
    private val logIdGenerator: LogIdGenerator
) {

    suspend fun activate() {
        val requestId = logIdGenerator.generate()
        val newMdc = mapOf(OPERATION_ID.value to requestId, OPERATION_NAME.value to SBERBANK_PAYMENT.value)
        withContext(MDCContext(MDC.getCopyOfContextMap() + newMdc)) {
            // logic will be here
        }
    }
}