package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.CCB
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbResponseError
import ru.catcab.taximaster.paymentgateway.dto.ccb.CcbResponsePayment
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.RECEIVER
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.CCB_PAYMENT
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.SECONDS

class CcbPaymentOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val paymentInOperation: PaymentInOperation
) {
    private val methodLogger = MethodLogger()

    suspend fun activate(account: String, amount: BigDecimal, payId: String, payDate: LocalDateTime): Any {
        methodLogger.logSuspendMethod(::activate, account, amount, payId, payDate) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to CCB_PAYMENT.value, RECEIVER.value to account)
        }?.let { return it() }

        val receiver = account.removeLeadingZeros()
        val payment = withContext(Dispatchers.IO) {
            transaction(database) {
                Payment.find { (Payments.payId eq payId) and (Payments.sourceType eq CCB)  }.firstOrNull()
            }
        }

        if (payment != null) {
            return CcbResponseError(30, "Был другой платеж с указанным номером")
        }

        val newPayment = paymentInOperation.activate(CCB, receiver, amount, payId, payDate, MDCKey.getValue(REQUEST_ID))
        val regTime = newPayment.inserted.truncatedTo(SECONDS)
        return CcbResponsePayment(0, "OK", account, newPayment.payId, regTime)
    }
}