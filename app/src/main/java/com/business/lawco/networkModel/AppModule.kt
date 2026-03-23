package com.business.lawco.networkModel

import android.content.Context
import android.util.Log
import com.business.lawco.AuthInterceptor
import com.business.lawco.networkModel.common.CommonRepository
import com.business.lawco.networkModel.common.CommonRepositoryImplement
import com.business.lawco.networkModel.forgotPassword.ForgotPasswordRepository
import com.business.lawco.networkModel.forgotPassword.ForgotPasswordRepositoryImplement
import com.business.lawco.networkModel.homeScreen.attorney.AttorneyHomeRepository
import com.business.lawco.networkModel.homeScreen.attorney.AttorneyHomeRepositoryImplement
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeRepository
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeRepositoryImplement
import com.business.lawco.networkModel.login.LoginRepository
import com.business.lawco.networkModel.login.LoginRepositoryImplement
import com.business.lawco.networkModel.paymentManagement.PaymentManagementRepository
import com.business.lawco.networkModel.paymentManagement.PaymentManagementRepositoryImplement
import com.business.lawco.networkModel.signUp.SignUpRepository
import com.business.lawco.networkModel.signUp.SignUpRepositoryImplement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.business.lawco.BuildConfig
import com.business.lawco.utility.AppConstant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMyApi(retrofit: Retrofit.Builder, okHttpClient: OkHttpClient): ApiRequest {
        return  retrofit.client(okHttpClient).build().create(ApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message -> Log.d("RetrofitLog", message) }
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }else{
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(AppConstant.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())


    @Provides
    @Singleton
    fun provideMyLoginRepository(api : ApiRequest) : LoginRepository {
        return LoginRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMySignUpRepository(api : ApiRequest) : SignUpRepository {
        return SignUpRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMyForgotPasswordRepository(api : ApiRequest) : ForgotPasswordRepository {
        return ForgotPasswordRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMyCommonRepository(api : ApiRequest) : CommonRepository {
        return CommonRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMyPaymentRepository(api : ApiRequest) : PaymentManagementRepository {
        return PaymentManagementRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMyHomeScreenRepository(api : ApiRequest) : AttorneyHomeRepository {
        return AttorneyHomeRepositoryImplement(api)
    }

    @Provides
    @Singleton
    fun provideMyConsumerHomeScreenRepository(api : ApiRequest) : ConsumerHomeRepository {
        return ConsumerHomeRepositoryImplement(api)
    }

}