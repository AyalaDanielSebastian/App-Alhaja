package com.example.alhaja.data.remote

import retrofit2.http.GET

data class ProductoRemotoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)

interface JoyasApi {
    @GET("products/category/jewelery")
    suspend fun obtenerJoyas(): List<ProductoRemotoDto>
}
