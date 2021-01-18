package ru.catcab.taximaster.paymentgateway.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.catcab.taximaster.paymentgateway.database.entity.CarDriverInfo
import ru.catcab.taximaster.paymentgateway.service.client.TaxiMasterApiClientAdapter
import ru.catcab.taximaster.paymentgateway.util.context.LogIdGenerator
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_ID
import ru.catcab.taximaster.paymentgateway.util.context.MDCKey.OPERATION_NAME
import ru.catcab.taximaster.paymentgateway.util.context.OperationNames.CAR_DRIVER_SYNC
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger
import java.math.MathContext
import java.math.RoundingMode

class CarDriverSynchronizationOperation(
    private val taxiMasterApiClientAdapter: TaxiMasterApiClientAdapter,
    private val database: Database,
    private val logIdGenerator: LogIdGenerator
) {
    private val methodLogger = MethodLogger()
    private val semaphore = Semaphore(1)

    companion object {
        private val MC18 = MathContext(18)
    }

    suspend fun activate() {
        methodLogger.logSuspendMethod(::activate) {
            mdc = mapOf(OPERATION_ID.value to logIdGenerator.generate(), OPERATION_NAME.value to CAR_DRIVER_SYNC.value)
        }?.let { return it() }

        semaphore.withPermit {
            val driversInfo = taxiMasterApiClientAdapter.getDriversInfo()
            val crewsInfos = taxiMasterApiClientAdapter.getCrewsInfo().sortedBy { it.crewId }

            val driversInfoMap = driversInfo.associateBy { it.driverId }

            val pairs = crewsInfos
                .filter { driversInfoMap.containsKey(it.driverId) }
                .map { it to driversInfoMap[it.driverId]!! }

            withContext(Dispatchers.IO) {
                transaction(database) {

                    val carDriverInfoMap = CarDriverInfo.all().associateBy { it.id.value }

                    val pairsMap = pairs.associateBy { it.first.crewId }

                    val newElements = pairsMap.keys.toSet().minus(carDriverInfoMap.keys)
//                val forDelete = carDriverInfoMap.keys.toMutableSet().apply { removeAll(tmIds) }

                    val checkForUpdate = pairsMap.keys.toMutableSet().apply { retainAll(carDriverInfoMap.keys) }

//                forDelete.map { carDriverInfoMap[it]!! }.forEach { it.delete() }


                    newElements.map { pairsMap[it]!! }.forEach { (crew, driver) ->
                        CarDriverInfo.new(crew.crewId) {
                            carId = crew.carId
                            driverId = crew.driverId
                            code = crew.code
                            fio = driver.name
                            balance = driver.balance.toBigDecimal(MC18).setScale(2, RoundingMode.HALF_UP)
                        }
                    }

                    checkForUpdate.map { carDriverInfoMap[it]!! to pairsMap[it]!! }.forEach { (db, pair) ->
                        val (crew, driver) = pair
                        if (db.carId != crew.carId) db.carId = crew.carId
                        if (db.driverId != crew.driverId) db.driverId = crew.driverId
                        if (db.code != crew.code) db.code = crew.code
                        if (db.fio != driver.name) db.fio = driver.name
                        val driverBalance = driver.balance.toBigDecimal(MC18).setScale(2, RoundingMode.HALF_UP)
                        if (db.balance.compareTo(driverBalance) != 0) db.balance = driverBalance
                    }
                }
            }
        }
    }
}