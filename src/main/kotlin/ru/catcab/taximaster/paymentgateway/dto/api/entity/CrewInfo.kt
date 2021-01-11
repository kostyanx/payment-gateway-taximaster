package ru.catcab.taximaster.paymentgateway.dto.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CrewInfo (
    @SerialName("crew_id")
    val crewId: Int, //ИД экипажа
    @SerialName("code")
    val code: String, // Позывной экипажа
    @SerialName("name")
    val name: String, // Наименование экипажа
    @SerialName("driver_id")
    val driverId: Int, // ИД водителя
    @SerialName("car_id")
    val carId: Int, // ИД автомобиля
    @SerialName("crew_group_id")
    val crewGroupId: Int, // ИД группы экипажа
    @SerialName("crew_state_id")
    val crewStateId: Int, // ИД состояния экипажа
    @SerialName("online")
    val online: Boolean, // Водитель подключен к серверу «Связи с водителями»
    @SerialName("work_shift_sum")
    val workShiftSum: Double, // Сумма, списываемая за смену
    @SerialName("min_balance")
    val minBalance: Double, // Минимальный баланс, при котором можно выйти на смену
    @SerialName("common_priority")
    val commonPriority: Int, // Общий приоритет
    @SerialName("static_priority")
    val staticPriority: Int, // Статический приоритет
    @SerialName("dynamic_priority")
    val dynamicPriority: Int, // Динамический приоритет
    @SerialName("rating_priority")
    val ratingPriority: Int, // Приоритет по рейтингу
    @SerialName("order_change_id")
    val orderChangeId: Int, // Индивидуальная сдача с заказа
    @SerialName("has_light_house")
    val hasLightHouse: Boolean, // Шашка
    @SerialName("has_label")
    val hasLabel: Boolean, // Наклейка
    @SerialName("use_plan_shifts")
    val usePlanShifts: Boolean, // Запрет работы вне запланированных смен
    @SerialName("order_params")
    val orderParams: List<Int>, // Массив параметров заказа экипажа // ИД параметра заказа
    @SerialName("attribute_values")
    val attributeValues: List<Attribute> = emptyList() // Массив параметров заказа экипажа // ИД параметра заказа
)