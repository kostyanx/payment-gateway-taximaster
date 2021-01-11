package ru.catcab.taximaster.paymentgateway.dto.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverInfo(
    @SerialName("driver_id")
    val driverId: Int, // ИД водителя
    @SerialName("name")
    val name: String, // ФИО водителя
    @SerialName("balance")
    val balance: Double, // Баланс водителя
    @SerialName("birthday")
    val birthday: String?, // ДД.ММ.ГГГГ День рождения водителя
    @SerialName("car_id")
    val carId: Int, // ИД основного автомобиля водителя
    @SerialName("license")
    val license: String, // Номер лицензии на перевозку (разрешения на перевозку)
    @SerialName("home_phone")
    val homePhone: String, // Любой неосновной телефон водителя (устаревшее поле)
    @SerialName("mobile_phone")
    val mobilePhone: String, // Основной телефон водителя (устаревшее поле)
    @SerialName("is_locked")
    val isLocked: Boolean, // Водитель заблокирован
    @SerialName("is_dismissed")
    val isDismissed: Boolean, // Водитель уволен
    @SerialName("driver_photo")
    val driverPhoto: String? = null, // Фото водителя (только если need_photo = true) - только для отдельного запроса по водителю
    @SerialName("order_params")
    val orderParams: List<Int>,
    @SerialName("phones")
    val phones: List<Phone>,
    @SerialName("term_account")
    val termAccount: String, // Терминальный аккаунт
    @SerialName("name_for_taxophone")
    val nameForTaxophone: String? = null,
    @SerialName("accounts")
    val accounts: List<Account>? = null, // Массив балансов счетов - только для отдельного запроса по водителю
    @SerialName("attribute_values")
    val attributeValues: List<Attribute> = emptyList() // Массив значений атрибутов
)
