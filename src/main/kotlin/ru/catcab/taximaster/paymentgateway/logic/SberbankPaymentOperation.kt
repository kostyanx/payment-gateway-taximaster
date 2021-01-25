package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Payment
import ru.catcab.taximaster.paymentgateway.database.entity.Payments
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASH
import ru.catcab.taximaster.paymentgateway.database.enum.SourceType.SBERBANK_CASHLESS
import ru.catcab.taximaster.paymentgateway.dto.sberbank.PaymentResponse
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.removeLeadingZeros
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.REQUEST_ID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.temporal.ChronoUnit.SECONDS

class SberbankPaymentOperation(
    private val database: Database,
    private val paymentInOperation: PaymentInOperation
) {

    suspend fun activate(account: String, amount: String, payId: String, payDate: String, payCh: String?): PaymentResponse {
        val receiver = account.removeLeadingZeros()
        val amountVal = amount.toBigDecimal()
        val payDateVal = LocalDateTime.parse(payDate, ISO_LOCAL_DATE_TIME)
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

        val sourceType = when(payCh) {
            "KASSA", "US_N", "AGENT" -> SBERBANK_CASH
            "KASSA_V", "F_190", "US_V", "SBOL", "MOB", "APAY" -> SBERBANK_CASHLESS
            else -> SBERBANK_CASHLESS
        }
        val newPayment = paymentInOperation.activate(sourceType, receiver, amountVal, payId, payDateVal, MDCKey.getValue(REQUEST_ID))
        val now = LocalDateTime.now().truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
        val regTime = newPayment.inserted.truncatedTo(SECONDS).format(ISO_LOCAL_DATE_TIME)
        return PaymentResponse(0, "OK", regTime, now)
    }
}