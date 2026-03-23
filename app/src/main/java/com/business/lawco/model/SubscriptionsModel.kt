package com.business.lawco.model

data class SubscriptionsModel(
    var message :String,
    var status :Boolean,
    var data : ArrayList<SubcriptionData>

)

data class SubcriptionData(
    var id : Int ,
    var title: String,
    var price: String,
    var description: ArrayList<String>,
    var price_show: Int? = 0,
    var is_active: Boolean
)


