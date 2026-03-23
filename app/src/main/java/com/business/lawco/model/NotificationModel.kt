package com.business.lawco.model

data class NotificationModel (
    var message: String,
    var status: Boolean,
    var data: ArrayList<NotificationData>
)

data class NotificationData(
    val message : String,
    val notification_read_status : String,
    val profile_picture : String,
    val formatted_date : String?,
    val formatted_time : String?,
    val created_at : String?,
)