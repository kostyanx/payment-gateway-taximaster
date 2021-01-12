package ru.catcab.taximaster.paymentgateway.service.client

import ru.catcab.taximaster.paymentgateway.dto.api.enums.OperType
import ru.catcab.taximaster.paymentgateway.dto.api.queries.*
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

    suspend fun getCrewsInfo(): GetCrewsInfoResponse {
        methodLogger.logSuspendMethod(this::getCrewsInfo)?.let { return it() }
        return taxiMasterApiClient.getCrewsInfo()
    }

    suspend fun getCrewInfo(id: Int): GetCrewInfoResponse {
        methodLogger.logSuspendMethod(this::getCrewInfo, id)?.let { return it() }
        return taxiMasterApiClient.getCrewInfo(id)
    }

    suspend fun getDriverInfo(id: Int): GetDriverInfoResponse {
        methodLogger.logSuspendMethod(this::getDriverInfo, id)?.let { return it() }
        return taxiMasterApiClient.getDriverInfo(id)
    }

    suspend fun getDriversInfo(lockedDrivers: Boolean = false, dismissedDrivers: Boolean = false): GetDriversInfoResponse {
        methodLogger.logSuspendMethod(this::getDriversInfo, lockedDrivers, dismissedDrivers)?.let { return it() }
        return taxiMasterApiClient.getDriversInfo(lockedDrivers, dismissedDrivers)
    }
}