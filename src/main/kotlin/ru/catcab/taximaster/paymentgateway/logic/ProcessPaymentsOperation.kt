package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.Status.*
import ru.catcab.taximaster.paymentgateway.dto.api.enums.OperType.RECEIPT
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClientAdapter
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PROCESS_PAYMENTS
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.time.LocalDateTime

class ProcessPaymentsOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val taxiMasterApiClientAdapter: TaxiMasterApiClientAdapter
) {

    companion object {
        val LOG = LoggerFactory.getLogger(ProcessPaymentsOperation::class.java)!!
    }

    private val methodLogger = MethodLogger()

    fun activate() {
        methodLogger.logMethod(::activate) {
            returnVal = false
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PROCESS_PAYMENTS.value)
        }?.let { return it() }

        transaction(database) {
            val payments = Payment.find { Payments.status eq NEW }
            for (payment in payments) {
                payment.apply { LOG.info("payment: id={} sourceType={} payId={} receiver={} amount={} requestId={}", id, sourceType, payId, receiver, amount, requestId) }
                try {
                    val driverId = payment.receiver.toInt()
                    val operId = runBlocking {
                        taxiMasterApiClientAdapter.createDriverOperation(driverId, payment.amount.toDouble(), RECEIPT, "Платеж через ${payment.sourceType}").data.operId
                    }
                    payment.status = SUCCESS
                    payment.operId = operId
                    payment.updated = LocalDateTime.now()
                } catch (e: Throwable) {
                    payment.counter = payment.counter + 1
                    payment.status = RETRY
                    payment.errorMessage = e.toString()
                    payment.updated = LocalDateTime.now()
                }
            }

        }

    }
}