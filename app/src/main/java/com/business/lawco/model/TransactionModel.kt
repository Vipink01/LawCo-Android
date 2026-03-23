package com.business.lawco.model

data class TransactionModel(
    val message : String,
    val status : Boolean,
    val data : ArrayList<TransactionDetail>?
)

data class TransactionDetail(
    val id : Int ,
    val user_management_id : Int ,
    val subscription_type : String ,
    val credit : Int ,
    val status : Int ,
    val price : String? ,
    val created_at : String ,
    val name : String ,
    val formatted_date : String ,
    val formatted_time : String
  //  val user : UserDetail
)

data class UserDetail(
    val id : Int,
    val profile_picture : String,
    val full_name : String,
)
