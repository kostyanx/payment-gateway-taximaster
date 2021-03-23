package ru.catcab.taximaster.paymentgateway.dto.ccb

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import java.time.LocalDateTime

@JsonRootName("response")
data class CcbResponsePayment(
    @JsonProperty("err_code")
    val errCode: Int,
    @JsonProperty("err_text")
    val errText: String,
    @JsonProperty("account")
    val account: String,
    @JsonProperty("reg_id")
    val regId: String,
    @JsonProperty("reg_date")
    val regDate: LocalDateTime
)
