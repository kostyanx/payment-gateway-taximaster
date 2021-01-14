package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Driver(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Driver>(Drivers)
    var fio by Drivers.fio
    var balance by Drivers.balance
}