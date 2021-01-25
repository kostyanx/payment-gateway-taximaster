package ru.catcab.taximaster.paymentgateway.dto.taximaster.queries

import kotlinx.serialization.Serializable
import ru.catcab.taximaster.paymentgateway.dto.taximaster.entity.CrewInfo

@Serializable
data class GetCrewInfoResponse(
    override val code: Int,
    override val descr: String,
    override val data: CrewInfo
) : CommonResponse<CrewInfo>