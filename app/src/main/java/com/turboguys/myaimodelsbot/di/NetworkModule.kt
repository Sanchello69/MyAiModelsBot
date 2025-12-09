package com.turboguys.myaimodelsbot.di

import android.util.Base64
import com.turboguys.myaimodelsbot.BuildConfig
import com.turboguys.myaimodelsbot.data.remote.OpenRouterApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val API_KEY_QUALIFIER = "api_key"

val networkModule = module {

    single(named(API_KEY_QUALIFIER)) {
        BuildConfig.OPENROUTER_API_KEY
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor { chain ->
                val apiKey = get<String>(named(API_KEY_QUALIFIER))
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("HTTP-Referer", "https://myaimodelsbot.app")
                    .addHeader("X-Title", "AI Models Bot")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(OpenRouterApiService::class.java)
    }
}
