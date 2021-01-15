package ru.catcab.taximaster.paymentgateway.database.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Payment(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Payment>(Payments)
    var sourceType by Payments.sourceType
    var payId by Payments.payId
    var receiver by Payments.receiver
    var amount by Payments.amount
    var payTimestamp by Payments.payTimestamp
    var requestId by Payments.requestId
    var operId by Payments.operId
    var status by Payments.status
    var counter by Payments.counter
    var errorMessage by Payments.errorMessage
    var inserted by Payments.inserted
    var updated by Payments.updated
}