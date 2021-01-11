package ru.catcab.taximaster.paymentgateway.dto.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attribute(
    @SerialName("id")
    val id: Int,
    @SerialName("bool_value")
    val boolValue: Boolean? = null,
    @SerialName("num_value")
    val numValue: Double? = null,
    @SerialName("str_value")
    val strValue: String? = null
)