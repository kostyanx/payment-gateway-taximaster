package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.id.IntIdTable

object Drivers : IntIdTable("DRIVERS", "DRIVER_ID") {
    val fio = varchar("FIO", 100)
    val balance = decimal("BALANCE", 18, 2)
}

