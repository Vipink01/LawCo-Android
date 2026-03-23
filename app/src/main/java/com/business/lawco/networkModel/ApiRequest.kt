package com.business.lawco.networkModel

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiRequest {

    @Headers("Accept: application/json")
    @POST("sign-in")
    @FormUrlEncoded
    fun signIn(
        @Field("email_phone") emailOrPhone: String,
        @Field("password") password: String,
        @Field("type") type: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("social-login")
    @FormUrlEncoded
    fun socialLogIn(
        @Field("name") name: String,
        @Field("email_phone") email: String,
        @Field("type") type: String,
        @Field("social_id") socialId: String,
        @Field("social") social: String,
    ): Call<JsonObject?>


    @Headers("Accept: application/json")
    @POST("sign-up")
    @FormUrlEncoded
    fun signUp(
        @Field("name") name: String,
        @Field("email_phone") emailOrPhone: String,
        @Field("password") password: String,
        @Field("type") type: String,
    ): Call<JsonObject?>


    @POST("get-sign-up-otp")
    @FormUrlEncoded
    fun signUpOtp(
        @Field("email_phone") emailOrPhone: String,
        @Field("type") type: String
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("get-forgot-password-otp")
    @FormUrlEncoded
    fun sendForgotVerificationOtp(
        @Field("email_phone") emailOrPhone: String,
        @Field("type") type: String,
    ): Call<JsonObject?>


    @Headers("Accept: application/json")
    @POST("send-otp")
    @FormUrlEncoded
    fun sendOtpEmailPhone(
        @Field("emailOrPhone") emailOrPhone: String,
        @Field("userType") type: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("cliamed-sent-otp")
    @FormUrlEncoded
    fun sendOtpClaimEmailPhone(
        @Field("email_phone") emailOrPhone: String,
        @Field("user_id") userID: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("update-claim-status")
    @FormUrlEncoded
    fun personaVerifyUser(
        @Field("user_id") userID: String,
        @Field("is_claimed") isClaimed: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("cliamed-verify-otp")
    @FormUrlEncoded
    fun otpClaimEmailPhoneVerify(
        @Field("email_phone") emailOrPhone: String,
        @Field("user_id") userID: String,
        @Field("otp") otp: String
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("verify-otp")
    @FormUrlEncoded
    fun otpEmailPhoneVerify(
        @Field("emailOrPhone") emailOrPhone: String,
        @Field("userType") type: String,
        @Field("otp") userOTP: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @GET("cliamed-attorney-list")
    fun searchAttorneyList(
        @Query("search") search: String
    ): Call<JsonObject>

    @Headers("Accept: application/json")
    @POST("reset-password")
    @FormUrlEncoded
    fun resetPassword(
        @Field("email_phone") emailOrPhone: String,
        @Field("password") password: String,
        @Field("confirm_password") passwordConfirmation: String,
    ): Call<JsonObject?>

    @GET("get-content")
    fun getContent(
        @Query("type") type: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("contact-us")
    @FormUrlEncoded
    fun contactUs(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("message") message: String,
    ): Call<JsonObject?>

    @GET("get-notification")
    fun getNotification(): Call<JsonObject?>

    @GET("logout")
    fun logOutAccount(): Call<JsonObject?>

    @GET("delete-account")
    fun deleteAccount(): Call<JsonObject?>

    @GET("get-user-profile")
    fun getUserProfile(): Call<JsonObject?>

    @GET("get-subscription")
    fun getSubcription(): Call<JsonObject?>

    @POST("edit-user-profile")
    @Multipart
    fun editUserProfile(
        @Part("full_name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("email") email: RequestBody,
        @Part("location") location: RequestBody,
        @Part("area_of_practice") areaOfPractice: RequestBody,
        @Part("attorney_registration_number") attorneyRegistrationNumber: RequestBody,
        @Part("about") about: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("type") type: RequestBody,
        @Part profile_picture: MultipartBody.Part?
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("save-card")
    @FormUrlEncoded
    fun saveCard(
        @Field("token") cardToken: String,
    ): Call<JsonObject?>

    @GET("get-all-saved-card-list")
    fun getAllCard(): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("remove-card")
    @FormUrlEncoded
    fun removeCard(
        @Field("card_id") cardId: String,
    ): Call<JsonObject?>

    @GET("get-credit")
    fun getCredit(): Call<JsonObject?>

    @GET("get-all-registered-location-list")
    fun getAllRegisterLocation(): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("attorney-list")
    @FormUrlEncoded
    fun getAttorneyList(
        @Field("is_connected") isConnected: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
       /* @Field("address") address: List<String>,*/
        @Field("area_of_practice[]") areaOfPractice: List<String>,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("request-to-attorney")
    @FormUrlEncoded
    fun sendRequestToAttorney(
        @Field("attorney_id") attorneyId: String,
        @Field("action") action: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @Multipart
    @POST("request-to-attorney")
    fun sendRequestToAttorneyWithDoc(
        @Part("attorney_id") attorneyId: RequestBody,
        @Part("action") action: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part("description") description: RequestBody,
        @Part files: List<MultipartBody.Part> // For multiple files
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("get-all-request")
    @FormUrlEncoded
    fun getAllRequest(
        @Field("status") status: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("respond-to-request")
    @FormUrlEncoded
    fun respondToRequest(
        @Field("request_id") requestId: String,
        @Field("status") action: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("set-online-status")
    @FormUrlEncoded
    fun setOnlineStatus(
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("change-notification-status")
    @FormUrlEncoded
    fun changeNotificationStatus(
        @Field("notification_status") notificationStatus: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("save-address")
    @FormUrlEncoded
    fun saveAddress(
        @Field("id") id: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("address_type") addressType: String,
        @Field("address") address: String,
    ): Call<JsonObject?>

    @GET("get-address")
    fun getAddress(): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("delete-address")
    @FormUrlEncoded
    fun deleteAddress(
        @Field("id") id: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("set-default-address")
    @FormUrlEncoded
    fun setDefaultAddress(
        @Field("id") id: String,
    ): Call<JsonObject?>

    @GET("get-online-status")
    fun getOnlineStatus(): Call<JsonObject?>

    @GET("get-category")
    fun getAreaOfPractice(): Call<JsonObject?>

    @GET("get-transection")
    fun getTransactionList(): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("purchase-subscription")
    @FormUrlEncoded
    fun paymentForSubcription(
        @Field("price") price: String,
        @Field("plan_id") planId: String,
        @Field("status") status: String,
        @Field("customer_id") customerId: String,
        @Field("token") token: String,
    ): Call<JsonObject?>

    @Headers("Accept: application/json")
    @POST("cancel-subscription")
    @FormUrlEncoded
    fun cancelSubscription(
        @Field("subscription_id") price: String,
    ): Call<JsonObject?>

}