package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.Driver
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClientAdapter
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.DRIVER_SYNC
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.math.MathContext
import java.math.RoundingMode

class DriverSynchronizationOperation(
    private val taxiMasterApiClientAdapter: TaxiMasterApiClientAdapter,
    private val database: Database,
    private val logIdGenerator: LogIdGenerator
) {
    private val methodLogger = MethodLogger()

    companion object {
        private val MC18 = MathContext(18)
    }

    fun activate() {
        methodLogger.logMethod(::activate) {
            returnVal = false
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to DRIVER_SYNC.value)
        }?.let { return it() }

        val driversInfos = runBlocking(MDCContext()) {
            taxiMasterApiClientAdapter.getDriversInfo()
        }

        val driversTmMap = driversInfos.associateBy { it.driverId }


        transaction(database) {

            val driversDbMap = Driver.all().associateBy { it.id.value }

            val newElements = driversTmMap.keys.toSet().minus(driversDbMap.keys)
            val forDelete = driversDbMap.keys.toSet().minus(driversTmMap.keys)

            val checkForUpdate = driversDbMap.keys.toMutableSet().apply { retainAll(driversTmMap.keys) }

            forDelete.map { driversDbMap[it]!! }.forEach { it.delete() }

            newElements.map { driversTmMap[it]!! }.forEach { driver ->
                Driver.new(driver.driverId) {
                    fio = driver.name
                    balance = driver.balance.toBigDecimal(MC18).setScale(2, RoundingMode.HALF_UP)
                }
            }

            checkForUpdate.map { driversDbMap[it]!! to driversTmMap[it]!! }.forEach { (db, tm) ->
                if (db.fio != tm.name) db.fio = tm.name
                val driverBalance = tm.balance.toBigDecimal(MC18).setScale(2, RoundingMode.HALF_UP)
                if (db.balance.compareTo(driverBalance) != 0) db.balance = driverBalance
            }
        }
    }
}