package ru.catcab.taximaster.paymentgateway.dto.api.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDriverOperationResponse(
    override val code: Int,
    override val descr: String,
    override val data: CreateDriverOperationResponseData = CreateDriverOperationResponseData()
) : CommonResponse<CreateDriverOperationResponse.CreateDriverOperationResponseData?> {
    @Serializable
    data class CreateDriverOperationResponseData(
        @SerialName("oper_id")
        val operId: Int = 0
    )

}