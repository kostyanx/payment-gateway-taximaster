package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PAYMENT_IN
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentInOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator
) {
    private val methodLogger = MethodLogger()

    suspend fun activate(sourceType: SourceType, receiver: String, amount: BigDecimal, payId: String, payTimestamp: LocalDateTime, requestId: String) {
        methodLogger.logSuspendMethod(::activate, sourceType, receiver, amount, payId, payTimestamp, requestId) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PAYMENT_IN.value)
        }?.let { return it() }

        withContext(Dispatchers.IO) {
            transaction(database) {
                Payment.new {
                    this.sourceType = sourceType
                    this.receiver = receiver
                    this.amount = amount
                    this.payId = payId
                    this.payTimestamp = payTimestamp
                    this.requestId = requestId
                }
            }
        }
    }
}