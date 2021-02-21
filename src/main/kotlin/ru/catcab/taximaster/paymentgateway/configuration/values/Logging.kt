package ru.catcab.taximaster.paymentgateway.configuration.values

data class Logging(
    val audit: Audit,
    val requestLog: RequestLog
) {
    data class RequestLog(
        val schedule: String,
        val maxAgeDays: Int
    )

    data class Audit(
        val templates: List<AuditLogTemplate>
    )
}