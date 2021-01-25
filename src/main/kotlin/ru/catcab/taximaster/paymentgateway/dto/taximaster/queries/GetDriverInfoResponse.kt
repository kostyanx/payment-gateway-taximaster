package ru.catcab.taximaster.paymentgateway.dto.taximaster.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.catcab.taximaster.paymentgateway.dto.taximaster.entity.DriverInfo

@Serializable
data class GetDriverInfoResponse (
    @SerialName("code")
    override val code: Int,
    @SerialName("descr")
    override val descr: String,
    @SerialName("data")
    override val data: DriverInfo
) : CommonResponse<DriverInfo>