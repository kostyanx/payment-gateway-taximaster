package ru.catcab.taximaster.paymentgateway.logic

import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PAYMENT_IN
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentInOperation(
    private val logIdGenerator: LogIdGenerator
) {
    private val methodLogger = MethodLogger()

    suspend fun activate(receiver: String, amount: BigDecimal, payId: String, payTimestamp: LocalDateTime) {
        methodLogger.logSuspendMethod(::activate, receiver, amount, payId, payTimestamp) {
            returnVal = false
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PAYMENT_IN.value)
        }?.let { return it() }
    }
}