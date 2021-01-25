package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.Status.NEW
import ru.catcab.taximaster.paymentgateway.database.enum.Status.RETRY
import ru.catcab.taximaster.paymentgateway.util.common.Strategy
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.PAYMENTS_POLLING
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger

class PaymentsPollingOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val paymentOutOperation: PaymentOutOperation,
    private val retryStrategy: Strategy
) {

    companion object {
        val LOG = LoggerFactory.getLogger(PaymentsPollingOperation::class.java)!!
    }

    private val methodLogger = MethodLogger()
    private val semaphore =  Semaphore(1)

    suspend fun activate() {
        methodLogger.logSuspendMethod(::activate) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to PAYMENTS_POLLING.value)
        }?.let { return it() }

        withContext(Dispatchers.IO) {
            semaphore.withPermit {
                transaction(database) {
                    Payments.slice(Payments.id, Payments.receiver, Payments.requestId)
                        .select { Payments.status eq NEW }.limit(10)
                        .map { Payment.wrapRow(it) }
                }.forEach {
                    paymentOutOperation.activate(it.id.value, it.receiver, it.requestId)
                }

                transaction(database) {
                    Payments.slice(Payments.id, Payments.counter, Payments.updated, Payments.receiver, Payments.requestId)
                        .select { Payments.status eq RETRY }
                        .filter { retryStrategy.retryRequired(it[Payments.counter], it[Payments.updated]) }
                        .map { Payment.wrapRow(it) }
                }.forEach {
                    paymentOutOperation.activate(it.id.value, it.receiver, it.requestId)
                }
            }
        }

    }
}