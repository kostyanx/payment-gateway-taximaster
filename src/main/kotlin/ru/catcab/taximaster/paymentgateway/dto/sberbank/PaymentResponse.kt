package ru.catcab.taximaster.paymentgateway.dto.sberbank

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("response")
data class PaymentResponse(
    @JsonProperty("CODE")
    val code: Int,
    @JsonProperty("MESSAGE")
    val message: String,
    @JsonProperty("REG_DATE")
    val regDate: String,
    @JsonProperty("SYS_DATE")
    val sysDate: String
)
