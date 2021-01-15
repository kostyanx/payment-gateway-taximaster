package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.Status.NEW
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PAYMENTS_POLLING
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger

class PaymentsPollingOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val paymentOutOperation: PaymentOutOperation
) {

    companion object {
        val LOG = LoggerFactory.getLogger(PaymentsPollingOperation::class.java)!!
    }

    private val methodLogger = MethodLogger()

    suspend fun activate() {
        methodLogger.logSuspendMethod(::activate) {
            returnVal = false
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PAYMENTS_POLLING.value)
        }?.let { return it() }

        val paymentIds = suspendedTransactionAsync(Dispatchers.IO, db = database) {
            Payments.slice(Payments.id)
                .select { Payments.status eq NEW }.limit(10)
                .map { it[Payments.id].value }
        }.await()

        paymentIds.forEach {
            paymentOutOperation.activate(it)
        }

    }
}