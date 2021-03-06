package ru.catcab.taximaster.paymentgateway.dto.taximaster.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account (
    @SerialName("account_kind")
    val accountKind: Int,
    @SerialName("balance")
    val balance: Double
)
