package ru.catcab.taximaster.paymentgateway.logic

import io.ktor.utils.io.errors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.enum.Status.*
import ru.catcab.taximaster.paymentgateway.dto.api.enums.OperType.RECEIPT
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClientAdapter
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.*
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PAYMENT_OUT
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.time.LocalDateTime

class PaymentOutOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val taxiMasterApiClientAdapter: TaxiMasterApiClientAdapter
) {

    companion object {
        val LOG = LoggerFactory.getLogger(PaymentOutOperation::class.java)!!
        fun Throwable.isRetryable(): Boolean {
            return this is IOException || this is EOFException
        }
    }

    private val methodLogger = MethodLogger()

    suspend fun activate(paymentId: Int, requestId: String) {
        methodLogger.logSuspendMethod(::activate, paymentId, requestId) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PAYMENT_OUT.value, REQUEST_ID.value to requestId)
        }?.let { return it() }

        withContext(Dispatchers.IO) {
            newSuspendedTransaction(db = database) {
                val payment = Payment.findById(paymentId)?.takeIf { it.status == NEW } ?: return@newSuspendedTransaction
                payment.apply { LOG.info("payment: id={} sourceType={} payId={} receiver={} amount={}", id, sourceType, payId, receiver, amount, requestId) }
                try {
                    val driverId = payment.receiver.toInt()
                    val operId = taxiMasterApiClientAdapter.createDriverOperation(driverId, payment.amount.toDouble(), RECEIPT, "Платеж через ${payment.sourceType}").data.operId
                    payment.status = SUCCESS
                    payment.operId = operId
                    payment.updated = LocalDateTime.now()
                } catch (e: Throwable) {
                    payment.counter = payment.counter + 1
                    payment.status = if (e.isRetryable()) RETRY else ERROR
                    payment.errorMessage = e.toString()
                    payment.updated = LocalDateTime.now()
                }
            }
        }

    }
}