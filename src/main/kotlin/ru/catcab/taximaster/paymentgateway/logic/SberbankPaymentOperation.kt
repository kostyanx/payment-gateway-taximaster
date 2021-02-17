package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.configuration.values.ApplicationConfig
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASH
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASHLESS
import ru.catcab.taximaster.paymentgateway.dto.sberbank.PaymentResponse
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.*
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.SBERBANK_PAYMENT
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.temporal.ChronoUnit.SECONDS

class SberbankPaymentOperation(
    private val config: ApplicationConfig,
    private val database: Database,
    private val logIdGenerator: LogIdGenerator,
    private val paymentInOperation: PaymentInOperation
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH:mm:ss")
    private val methodLogger = MethodLogger()

    suspend fun activate(account: String, amount: String, payId: String, payDate: String, payCh: String?): PaymentResponse {
        methodLogger.logSuspendMethod(::activate, account, amount, payId, payDate, payCh) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to SBERBANK_PAYMENT.value, RECEIVER.value to account)
        }?.let { return it() }

        val receiver = account.removeLeadingZeros()
        val amountVal = amount.toBigDecimal()
        val payDateVal = LocalDateTime.parse(payDate, dateTimeFormatter)
        val payment = withContext(Dispatchers.IO) {
            transaction(database) {
                Payment.find { (Payments.payId eq payId) and (Payments.sourceType inList listOf(SBERBANK_CASH, SBERBANK_CASHLESS))  }.firstOrNull()
            }
        }

        if (payment != null) {
            val now = LocalDateTime.now().truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
            val regTime = payment.inserted.truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
            return PaymentResponse(8, "Платеж уже был проведен", regTime, now)
        }

        val sourceType = when {
            config.sberbank.payChCash.contains(payCh) -> SBERBANK_CASH
            config.sberbank.payChCashless.contains(payCh) -> SBERBANK_CASHLESS
            else -> config.sberbank.payChDefault
        }
        val newPayment = paymentInOperation.activate(sourceType, receiver, amountVal, payId, payDateVal, MDCKey.getValue(REQUEST_ID))
        val now = LocalDateTime.now().truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
        val regTime = newPayment.inserted.truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
        return PaymentResponse(0, "OK", regTime, now)
    }
}