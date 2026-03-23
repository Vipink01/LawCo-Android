package com.business.lawco.model.consumer


data class SelectAreaOfPracticeModel(
    var id:Int,
    var areaOfPractice: String,
    var status: Boolean = false
)