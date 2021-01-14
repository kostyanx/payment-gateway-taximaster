package ru.catcab.taximaster.paymentgateway.service.client

import ru.catcab.taximaster.paymentgateway.dto.api.entity.CrewInfo
import ru.catcab.taximaster.paymentgateway.dto.api.entity.DriverInfo
import ru.catcab.taximaster.paymentgateway.dto.api.enums.OperType
import ru.catcab.taximaster.paymentgateway.dto.api.queries.CreateDriverOperationResponse
import ru.catcab.taximaster.paymentgateway.util.logging.MethodLogger

class TaxiMasterApiClientAdapter(
    private val taxiMasterApiClient: TaxiMasterApiClient
) {
    private val methodLogger = MethodLogger()

    suspend fun createDriverOperation(
        driverId: Int,
        operSum: Double,
        operType: OperType,
        name: String? = null,
        comment: String? = null,
        accountKind: Int = 0
    ): CreateDriverOperationResponse {
        methodLogger.logSuspendMethod(this::createDriverOperation, driverId, operSum, operType, name, comment, accountKind)?.let { return it() }
        return taxiMasterApiClient.createDriverOperation(driverId, operSum, operType, name, comment, accountKind)
    }

    suspend fun getCrewsInfo(): List<CrewInfo> {
        methodLogger.logSuspendMethod(this::getCrewsInfo) { returnVal = false }?.let { return it() }
        return taxiMasterApiClient.getCrewsInfo().data.crewsInfo
    }

    suspend fun getCrewInfo(id: Int): CrewInfo {
        methodLogger.logSuspendMethod(this::getCrewInfo, id)?.let { return it() }
        return taxiMasterApiClient.getCrewInfo(id).data
    }

    suspend fun getDriverInfo(id: Int): DriverInfo {
        methodLogger.logSuspendMethod(this::getDriverInfo, id)?.let { return it() }
        return taxiMasterApiClient.getDriverInfo(id).data
    }

    suspend fun getDriversInfo(lockedDrivers: Boolean = false, dismissedDrivers: Boolean = false): List<DriverInfo> {
        methodLogger.logSuspendMethod(this::getDriversInfo, lockedDrivers, dismissedDrivers) { returnVal = false }?.let { return it() }
        return taxiMasterApiClient.getDriversInfo(lockedDrivers, dismissedDrivers).data.driversInfo
    }
}