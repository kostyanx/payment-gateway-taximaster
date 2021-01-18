package ru.catcab.taximaster.paymentgateway.util.context

enum class OperationNames(
    val value: String
) {
    CAR_DRIVER_SYNC("car_driver_sync"),
    DRIVER_SYNC("driver_sync"),
    PAYMENTS_POLLING("payments_polling"),
    PAYMENT_IN("payment_in"),
    PAYMENT_OUT("payment_out"),
    FLYWAY_MIGRATION("flyway_migration");
}