package ru.catcab.taximaster.paymentgateway.dto.ccb

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@JsonRootName("request")
data class CcbRequest(
    @JsonProperty("params")
    val params: Params,
    @JsonProperty("sign")
    val sign: String
) {
    companion object {
        @JvmStatic val ACTION_CHECK = 1
        @JvmStatic val ACTION_PAYMENT = 2
    }

    data class Params (
        @JsonProperty("act")
        val act: Int?,
        @JsonProperty("account")
        val account: String?,
        @JsonProperty("agent_date")
        val agentDate: LocalDateTime?,
        @JsonProperty("pay_amount")
        val payAmount: BigDecimal?,
        @JsonProperty("pay_id")
        val payId: String?,
        @JsonProperty("pay_date")
        val payDate: LocalDateTime?,
        @JsonProperty("pay_type")
        val payType: Int?,
        @JsonProperty("serv_code")
        val servCode: String?
    )
}
