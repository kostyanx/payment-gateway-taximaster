package ru.catcab.taximaster.paymentgateway.dto.sberbank

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import java.math.BigDecimal

@JsonRootName("response")
data class CheckResponseOk(
    @JsonProperty("CODE")
    val code: Int,
    @JsonProperty("MESSAGE")
    val message: String,
    @JsonProperty("FIO")
    val fio: String,
    @JsonProperty("ADDRESS")
    val address: String,
    @JsonProperty("ACCOUNT_BALANCE")
    val accountBalance: BigDecimal
)
