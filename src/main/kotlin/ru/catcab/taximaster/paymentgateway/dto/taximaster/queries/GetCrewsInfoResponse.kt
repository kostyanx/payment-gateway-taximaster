package ru.catcab.taximaster.paymentgateway.dto.taximaster.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.catcab.taximaster.paymentgateway.dto.taximaster.entity.CrewInfo

@Serializable
data class GetCrewsInfoResponse(
    @SerialName("code")
    override val code: Int,
    @SerialName("descr")
    override val descr: String,
    @SerialName("data")
    override val data: Data,
) : CommonResponse<GetCrewsInfoResponse.Data> {

    @Serializable
    data class Data (
        @SerialName("crews_info")
        val crewsInfo: List<CrewInfo> = emptyList()
    )

}