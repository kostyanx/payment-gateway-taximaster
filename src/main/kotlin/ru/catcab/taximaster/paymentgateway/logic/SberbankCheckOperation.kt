package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Driver
import ru.catcab.taximaster.paymentgateway.dto.sberbank.CheckResponseOk
import ru.catcab.taximaster.paymentgateway.exception.SberbankException
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.*
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.SBERBANK_CHECK
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger

class SberbankCheckOperation(
    private val database: Database,
    private val logIdGenerator: LogIdGenerator
) {
    companion object {
        @JvmStatic val NUMBER_REGEX = "^[0-9]{1,10}$".toRegex()
        @JvmStatic val ACCOUNT_MUST_BE_A_NUMBER = "ИД Водителя должно быть числом от 1 до 2147483647"
        @JvmStatic val ACCOUNT_NOT_FOUND = "Водитель с таким ИД не найден"
    }

    private val methodLogger = MethodLogger()

    suspend fun activate(account: String): CheckResponseOk {
        methodLogger.logSuspendMethod(::activate, account) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to SBERBANK_CHECK.value, RECEIVER.value to account)
        }?.let { return it() }

        if (!account.matches(NUMBER_REGEX)) throw SberbankException(3, ACCOUNT_MUST_BE_A_NUMBER)

        val driverId = account.toLong()
            .takeIf { it >= Integer.MIN_VALUE && it <= Integer.MAX_VALUE }?.toInt()
            ?: throw throw SberbankException(3, ACCOUNT_MUST_BE_A_NUMBER)

        return withContext(Dispatchers.IO) {
            transaction(database) {
                val driver = Driver.findById(driverId) ?: throw SberbankException(3, ACCOUNT_NOT_FOUND)
                CheckResponseOk(0, "OK", driver.fio, "", driver.balance)
            }
        }
    }
}