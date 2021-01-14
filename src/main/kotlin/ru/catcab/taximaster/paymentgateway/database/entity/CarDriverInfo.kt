package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CarDriverInfo(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<CarDriverInfo>(CarDriverInfoTable)
    var carId by CarDriverInfoTable.carId
    var driverId by CarDriverInfoTable.driverId
    var code by CarDriverInfoTable.code
    var fio by CarDriverInfoTable.fio
    var balance by CarDriverInfoTable.balance
}