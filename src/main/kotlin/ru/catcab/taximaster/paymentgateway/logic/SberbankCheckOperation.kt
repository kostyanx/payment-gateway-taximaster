package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Driver
import ru.catcab.taximaster.paymentgateway.dto.sberbank.CheckResponseOk
import ru.catcab.taximaster.paymentgateway.exception.SberbankException

class SberbankCheckOperation(
    private val database: Database
) {
    companion object {
        @JvmStatic val NUMBER_REGEX = "[0-9]+".toRegex()
        @JvmStatic val ACCOUNT_MUST_BE_A_NUMBER = "ИД Водителя должно быть числом"
        @JvmStatic val ACCOUNT_NOT_FOUND = "Водитель с таким ИД не найден"
    }

    suspend fun activate(account: String): CheckResponseOk {
        if (!account.matches(NUMBER_REGEX)) throw SberbankException(3, ACCOUNT_MUST_BE_A_NUMBER)

        val driverId = account.toInt()

        return withContext(Dispatchers.IO) {
            transaction(database) {
                val driver = Driver.findById(driverId) ?: throw SberbankException(3, ACCOUNT_NOT_FOUND)
                CheckResponseOk(0, "OK", driver.fio, "", driver.balance)
            }
        }
    }
}