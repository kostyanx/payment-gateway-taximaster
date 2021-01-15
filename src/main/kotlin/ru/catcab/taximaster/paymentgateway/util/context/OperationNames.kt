package ru.catcab.taximaster.paymentgateway.util.context

enum class OperationNames(
    val value: String
) {
    SBERBANK_PAYMENT("sberbank_payment"),
    CAR_DRIVER_SYNC("car_driver_sync"),
    DRIVER_SYNC("driver_sync"),
    PROCESS_PAYMENTS("process_payments");
}