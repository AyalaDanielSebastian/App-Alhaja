package com.example.alhaja.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RedAlhaja {
    private const val BASE_URL = "https://fakestoreapi.com/"

    val api: JoyasApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoyasApi::class.java)
    }
}
