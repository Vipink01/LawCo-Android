package com.business.lawco.model.consumer

data class Data(
    var no:String,
    var locationStatus:Boolean=false,
    var address: String,
    var latitude: String,
    var longitude: String
)