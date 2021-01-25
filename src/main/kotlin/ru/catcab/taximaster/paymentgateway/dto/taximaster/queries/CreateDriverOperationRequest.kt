package ru.catcab.taximaster.paymentgateway.dto.taximaster.queries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.catcab.taximaster.paymentgateway.dto.taximaster.enums.OperType

@Serializable
data class CreateDriverOperationRequest(
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("oper_sum")
    val operSum: Double, // TODO replace to BigDecimal without quotes
    @SerialName("oper_type")
    val operType: OperType,
    val name: String? = null,
    val comment: String? = null,
    @SerialName("account_kind")
    val accountKind: Int = 0
)
