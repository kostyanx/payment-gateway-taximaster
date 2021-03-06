package ru.catcab.taximaster.paymentgateway.util.context

enum class OperationNames(
    val value: String
) {
    CAR_DRIVER_SYNC("car_driver_sync"),
    DRIVER_SYNC("driver_sync"),
    PAYMENTS_POLLING("payments_polling"),
    PAYMENT_IN("payment_in"),
    PAYMENT_OUT("payment_out"),
    SBERBANK_CHECK("sberbank_check"),
    SBERBANK_PAYMENT("sberbank_payment"),
    CCB_CHECK("ccb_check"),
    CCB_PAYMENT("ccb_payment"),
    FLYWAY_MIGRATION("flyway_migration"),
    REMOVE_OLD_DATA("remove_old_data");
}