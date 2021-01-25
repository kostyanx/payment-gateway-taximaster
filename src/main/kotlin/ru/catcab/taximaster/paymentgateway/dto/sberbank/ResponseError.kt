package ru.catcab.taximaster.paymentgateway.dto.sberbank

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("response")
data class ResponseError(
    @JsonProperty("CODE")
    val code: Int,
    @JsonProperty("MESSAGE")
    val message: String
)
