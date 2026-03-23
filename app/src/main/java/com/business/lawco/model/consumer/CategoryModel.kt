package com.business.lawco.model.consumer

data class CategoryModel(
    var message: String,
    var status: Boolean,
    var data: ArrayList<AreaOfPractice>,
    var notification:Int?=0
)

data class AreaOfPractice(
    var id : Int,
    var category_name :String,
    var category_image :String,
)
