package ru.catcab.taximaster.paymentgateway.dto.ccb

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import java.math.BigDecimal

@JsonRootName("response")
data class CcbResponseBalance(
    @JsonProperty("err_code")
    val errCode: Int,
    @JsonProperty("err_text")
    val errText: String,
    @JsonProperty("account")
    val account: String,
    @JsonProperty("client_name")
    val clientName: String,
    @JsonProperty("balance")
    val balance: BigDecimal
)
