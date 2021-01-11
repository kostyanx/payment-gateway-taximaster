package ru.catcab.taximaster.paymentgateway.dto.api.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.catcab.taximaster.paymentgateway.dto.api.entity.DriverInfo

@Serializable
data class GetDriversInfoResponse (
    @SerialName("code")
    override val code: Int,
    @SerialName("descr")
    override val descr: String,
    @SerialName("data")
    override val data: Data
) : CommonResponse<GetDriversInfoResponse.Data> {
    @Serializable
    data class Data (
        @SerialName("drivers_info")
        val driversInfo: List<DriverInfo>
    )
}