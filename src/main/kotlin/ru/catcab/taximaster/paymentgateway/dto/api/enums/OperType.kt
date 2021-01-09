package ru.catcab.taximaster.paymentgateway.dto.api.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OperType {
    @SerialName("receipt")
    RECEIPT,
    @SerialName("expense")
    EXPENSE,
    ;
}
