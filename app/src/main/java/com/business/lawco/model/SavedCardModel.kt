package com.business.lawco.model

data class SavedCardModel(
    var message :String,
    var status :Boolean,
    var data :ArrayList<CardDetails>,
)

data class CardDetails(
    val id: Int,
    val customer_id: String,
    val card_id: String,
    val cardholdername: String,
    val last4: String,
    val exp_month: String,
    val exp_year: String,
    val email: String,
    val brand: String,
    var selectCard: Boolean,
)
