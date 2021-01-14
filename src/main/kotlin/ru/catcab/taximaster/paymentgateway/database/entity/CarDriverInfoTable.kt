package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.id.IntIdTable

object CarDriverInfoTable : IntIdTable("CAR_DRIVER_INFO", "CREW_ID") {
    val carId = integer("CAR_ID")
    val driverId = integer("DRIVER_ID")
    val code = varchar("CODE", 10)
    val fio = varchar("FIO", 100)
    val balance = decimal("BALANCE", 18, 2)
}

