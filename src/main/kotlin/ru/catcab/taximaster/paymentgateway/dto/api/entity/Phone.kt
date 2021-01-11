package ru.catcab.taximaster.paymentgateway.dto.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Phone(
    @SerialName("phone")
    val phone: String, // Номер телефона
    @SerialName("is_default")
    val isDefault: Boolean, // Признак основного телефона
    @SerialName("use_for_call")
    val useForCall: Boolean // Использовать для отзвона
)
