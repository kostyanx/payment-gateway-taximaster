package ru.catcab.taximaster.paymentgateway.dto.ccb

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("response")
data class CcbResponseError(
    @JsonProperty("err_code")
    val errCode: Int,
    @JsonProperty("err_text")
    val errText: String
)
